package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.cashbackStrategy.CashbackStrategy;
import org.poo.cashbackStrategy.NrOfTransactionsStrategy;
import org.poo.cashbackStrategy.SpendingThresholdStrategy;
import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.FailOperation;
import org.poo.operationTypes.TransactionOperation;

import java.util.List;

public class SendMoneyCommand implements Command {

    private final ExchangeRateManager exchangeRateManager;
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public SendMoneyCommand(final ExchangeRateManager exchangeRateManager,
                            final ObjectMapper objectMapper, final ArrayNode output) {
        this.exchangeRateManager = exchangeRateManager;
        this.objectMapper = objectMapper;
        this.output = output;
    }

    @Override
    public void execute(final List<User> users, List<Commerciant> commerciants, final CommandInput command) {
        Account fromAccount = null;
        Account toAccount = null;
        String toAccountCommerciantIBAN = null;
        boolean receiverIsCommerciant = false;
        boolean receiverIsUser = false;
        User senderUser = null;

        // Find accounts and sender user
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    fromAccount = account;
                }
                if (account.getIban().equals(command.getReceiver())) {
                    toAccount = account;
                    receiverIsUser = true;
                }
            }
        }

        senderUser = findUserByEmail(users, command.getEmail());
        Commerciant potentialCommerciant = null;

        // Handle user not found case
        if (senderUser == null) {
            addOutputToJson("User not found", command.getTimestamp());
            return;
        }

        for (Commerciant commerciant : commerciants) {
            if (commerciant.getAccount().equals(command.getReceiver())) {
                toAccountCommerciantIBAN = commerciant.getAccount();
                receiverIsCommerciant = true;
                potentialCommerciant = commerciant;
            }
        }

        if (command.getTimestamp() == 248) {
            System.out.println("AAAAAAAAAAAAAAAA");
            System.out.println("From account is " + fromAccount);
            System.out.println("To account is " + toAccount);
        }
        // Handle account not found case
        if (fromAccount == null) {
            return;
        }

        if (toAccount == null && toAccountCommerciantIBAN == null) {
            addOutputToJson("User not found", command.getTimestamp());
            return;
        }

        if (receiverIsUser == true) {
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
                    command.getDescription(),
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
        } else {
            // Deduct funds from sender
            fromAccount.removeFunds(command.getAmount());

            // 1) Convert 'command.getAmount()' from fromAccount currency -> RON for commission/cashback
            double transactionAmountInRON = command.getAmount();
            if (!fromAccount.getCurrency().equalsIgnoreCase("RON")) {
                double exRate = exchangeRateManager.getExchangeRate(
                        fromAccount.getCurrency(),
                        "RON"
                );
                if (exRate != -1) {
                    transactionAmountInRON = command.getAmount() * exRate;
                }
            }

            // 2) Apply commission
            double commission = senderUser.applyCommission(transactionAmountInRON);

            // 3) Convert commission back to fromAccount currency
            double commissionInSenderCurrency = commission;
            if (!fromAccount.getCurrency().equalsIgnoreCase("RON")) {
                double reverseExRate = exchangeRateManager.getExchangeRate("RON", fromAccount.getCurrency());
                if (reverseExRate != -1) {
                    commissionInSenderCurrency = commission * reverseExRate;
                } else {
                    commissionInSenderCurrency = 0.0;
                }
            }

            fromAccount.removeFunds(commissionInSenderCurrency);

            double cashback = 0.0;
            CashbackStrategy cashbackStrategy;
            if(potentialCommerciant.getCashbackType().equalsIgnoreCase("nrOfTransactions")) {
                cashbackStrategy = new NrOfTransactionsStrategy(fromAccount);
                cashback = cashbackStrategy.calculateCashback(
                        command.getAmount(),
                        potentialCommerciant.getCategory(),
                        potentialCommerciant.incrementAndGetTransactionCount(fromAccount),
                        0.0
                );
            } else {
                cashbackStrategy = new SpendingThresholdStrategy(senderUser);
                cashback = cashbackStrategy.calculateCashback(
                        command.getAmount(),
                        potentialCommerciant.getCategory(),
                        0,
                        transactionAmountInRON
                );
            }

            // Add funds to receiver
            fromAccount.addFunds(cashback);

            // Create transaction operation for sender
            TransactionOperation senderTransaction = new TransactionOperation(
                    command.getTimestamp(),
                    command.getDescription(),
                    fromAccount.getIban(),
                    toAccountCommerciantIBAN,
                    command.getAmount(),
                    fromAccount.getCurrency(),
                    "sent"
            );

            fromAccount.addOperation(senderTransaction);
            fromAccount.addCommerciantTransaction(potentialCommerciant.getName(), command.getAmount(), senderUser.getEmail());
        }
    }



    /**
     * Adds output JSON for failure cases.
     */
    private void addOutputToJson(String description, int timestamp) {
        ObjectNode outputNode = objectMapper.createObjectNode();
        ObjectNode detailsNode = objectMapper.createObjectNode();

        outputNode.put("command", "sendMoney");
        detailsNode.put("description", description);
        detailsNode.put("timestamp", timestamp);
        outputNode.set("output", detailsNode);
        outputNode.put("timestamp", timestamp);

        output.add(outputNode);
    }

    private User findUserByEmail(List<User> users, String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}
