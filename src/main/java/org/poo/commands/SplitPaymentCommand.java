package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.SplitPaymentFailOperation;
import org.poo.operationTypes.SplitPaymentOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitPaymentCommand implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;
    private final ExchangeRateManager exchangeRateManager;

    public SplitPaymentCommand(final ObjectMapper objectMapper, final ArrayNode output,
                               final ExchangeRateManager exchangeRateManager) {
        this.objectMapper = objectMapper;
        this.output = output;
        this.exchangeRateManager = exchangeRateManager;
    }

    /***
     * Handling the SplitPaymentCommand
     * Finding the accounts that should pay
     * Then of course I have to split the sum but thats not it
     * cause its possible to have acc of different currencies and
     * I have to convert accordingly
     * And I also check if all the accounts have sufficient money
     * DISCLAIMER : I DONT UNDERSTAND WHY IN AN INSUFFICIENT FUNDS ERROR
     * I HAVE TO PRINT THE LAST ACCOUNT SEEN THAT DOES NOT HAVE FUNDS ITS A TOTAL NONSENSE
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final CommandInput command) {
        List<String> accountsForSplit = command.getAccounts();
        double totalAmount = command.getAmount();
        String splitCurrency = command.getCurrency();
        int timestamp = command.getTimestamp();

        List<Account> participatingAccounts = new ArrayList<>();
        boolean sufficientFunds = true;
        String failedAccount = null;

        // Finding the acc
        for (String iban : accountsForSplit) {
            boolean accountFound = false;
            for (User user : users) {
                for (Account account : user.getAccounts()) {
                    if (account.getIban().equals(iban)) {
                        participatingAccounts.add(account);
                        accountFound = true;
                        break;
                    }
                }
                if (accountFound) {
                    break;
                }
            }
            if (!accountFound) {
                sufficientFunds = false;
                failedAccount = iban;
                break;
            }
        }

        // Conversion and split
        double equalShare = totalAmount / participatingAccounts.size();
        Map<Account, Double> convertedAmounts = new HashMap<>();

        for (Account account : participatingAccounts) {
            double convertedShare = equalShare;

            if (!account.getCurrency().equals(splitCurrency)) {
                double rate = exchangeRateManager.
                        getExchangeRate(splitCurrency, account.getCurrency());
                if (rate == -1) {
                    sufficientFunds = false;
                    failedAccount = account.getIban();
                    break;
                }
                convertedShare *= rate;
            }

            convertedAmounts.put(account, convertedShare);

            // Verify funds
            if (account.getBalance() < convertedShare) {
                sufficientFunds = false;
                failedAccount = account.getIban();
            }
        }

        // Creating an array of involved accounts
        ArrayNode involvedAccounts = objectMapper.createArrayNode();
        for (String accountIban : accountsForSplit) {
            involvedAccounts.add(accountIban);
        }

        // Error for insufficient funds
        if (!sufficientFunds) {
            SplitPaymentFailOperation failOperation = new SplitPaymentFailOperation(
                    timestamp,
                    "Split payment of " + String.format("%.2f", totalAmount) + " " + splitCurrency,
                    "Account " + failedAccount + " has insufficient funds for a split payment.",
                    involvedAccounts,
                    equalShare,
                    splitCurrency
            );

            for (Account account : participatingAccounts) {
                account.addOperation(failOperation);
            }
        } else {
            // Successful funds distribution
            for (Account account : participatingAccounts) {
                double convertedShare = convertedAmounts.get(account);
                account.removeFunds(convertedShare);

                SplitPaymentOperation splitPaymentOperation = new SplitPaymentOperation(
                        timestamp,
                        equalShare,
                        splitCurrency,
                        "Split payment of " + String.
                                format("%.2f", totalAmount) + " " + splitCurrency,
                        involvedAccounts
                );
                account.addOperation(splitPaymentOperation);
            }
        }
    }
}
