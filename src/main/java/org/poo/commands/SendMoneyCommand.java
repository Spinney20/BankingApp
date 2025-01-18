package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.FailOperation;
import org.poo.operationTypes.TransactionOperation;

import java.util.List;

public class SendMoneyCommand implements Command {

    private final ExchangeRateManager exchangeRateManager;

    public SendMoneyCommand(final ExchangeRateManager exchangeRateManager) {
        this.exchangeRateManager = exchangeRateManager;
    }

    @Override
    public void execute(final List<User> users, final CommandInput command) {
        Account fromAccount = null;
        Account toAccount = null;
        User senderUser = null;

        // Find accounts and sender user
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    fromAccount = account;
                    senderUser = user;
                }
                if (account.getIban().equals(command.getReceiver())) {
                    toAccount = account;
                }
            }
        }

        if (fromAccount == null || toAccount == null) {
            return; // Accounts not found; no further action
        }

        // Convert the amount to the receiver's currency
        double exchangeRate = exchangeRateManager.
                getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency());
        double convertedAmount = command.getAmount() * exchangeRate;

        // Convert the transaction amount to RON for commission calculation
        double transactionAmountInRON = convertedAmount; // Default to converted amount
        if (!toAccount.getCurrency().equals("RON")) {
            double reverseExchangeRate = exchangeRateManager.getExchangeRate(toAccount.getCurrency(), "RON");
            if (reverseExchangeRate != -1) {
                transactionAmountInRON = convertedAmount * reverseExchangeRate; // Convert to RON
            }
        }

        // Calculate commission based on the amount in RON
        double commission = senderUser.applyCommission(transactionAmountInRON);

        // Convert commission back to the sender's account currency
        double commissionInSenderCurrency = commission;
        if (!fromAccount.getCurrency().equalsIgnoreCase("RON")) {
            double reverseExchangeRate = exchangeRateManager.getExchangeRate("RON", fromAccount.getCurrency());
            if (reverseExchangeRate != -1) {
                commissionInSenderCurrency = commission * reverseExchangeRate;
            } else {
                commissionInSenderCurrency = 0.0; // If exchange rate is not available, treat commission as 0
            }
        }

        // Total amount to deduct (transaction + converted commission)
        double totalAmountToDeduct = command.getAmount() + commissionInSenderCurrency;

        // Check for sufficient funds
        if (fromAccount.getBalance() < totalAmountToDeduct) {
            FailOperation insufficientFundsOperation = new FailOperation(
                    command.getTimestamp(),
                    "Insufficient funds"
            );
            fromAccount.addOperation(insufficientFundsOperation);
            return;
        }

        // Deduct funds from sender and add funds to receiver
        fromAccount.removeFunds(totalAmountToDeduct);
        toAccount.addFunds(convertedAmount);

        // Create sender transaction operation with commission
        TransactionOperation senderTransaction = new TransactionOperation(
                command.getTimestamp(),
                command.getDescription() ,
                fromAccount.getIban(),
                toAccount.getIban(),
                command.getAmount(),
                fromAccount.getCurrency(),
                "sent"
        );

        // Create receiver transaction operation
        TransactionOperation receiverTransaction = new TransactionOperation(
                command.getTimestamp(),
                command.getDescription(),
                fromAccount.getIban(),
                toAccount.getIban(),
                convertedAmount,
                toAccount.getCurrency(),
                "received"
        );

        // Add operations to accounts
        fromAccount.addOperation(senderTransaction);
        toAccount.addOperation(receiverTransaction);
    }
}
