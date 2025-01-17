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

    /***
     * Handling the send money command
     * I search for the both accounts from and to
     * Then i check if the from has enough money and if not
     * adding a fail operation
     * If its ok I add operations for both the receiver and sender
     * And perform the transactions by adding to the receiver and
     * removing from the sender the amount of course exchenged to the
     * correct currencies
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final CommandInput command) {
        Account fromAccount = null;
        Account toAccount = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    fromAccount = account;
                }
                if (account.getIban().equals(command.getReceiver())) {
                    toAccount = account;
                }
            }
        }

        if (fromAccount != null && toAccount != null) {
            if (fromAccount.getBalance() < command.getAmount()) {
                FailOperation insufficientFundsOperation = new FailOperation(
                        command.getTimestamp(),
                        "Insufficient funds"
                );
                fromAccount.addOperation(insufficientFundsOperation);
                return;
            }
            double exchangeRate = exchangeRateManager.
                    getExchangeRate(fromAccount.getCurrency(), toAccount.getCurrency());
            double convertedAmount = command.getAmount() * exchangeRate;

            fromAccount.removeFunds(command.getAmount());
            toAccount.addFunds(convertedAmount);

            TransactionOperation senderTransaction = new TransactionOperation(
                    command.getTimestamp(),
                    command.getDescription(),
                    fromAccount.getIban(),
                    toAccount.getIban(),
                    command.getAmount(),
                    fromAccount.getCurrency(),
                    "sent"
            );

            TransactionOperation receiverTransaction = new TransactionOperation(
                    command.getTimestamp(),
                    command.getDescription(),
                    fromAccount.getIban(),
                    toAccount.getIban(),
                    convertedAmount,
                    toAccount.getCurrency(),
                    "received"
            );

            fromAccount.addOperation(senderTransaction);
            toAccount.addOperation(receiverTransaction);
        }
    }
}
