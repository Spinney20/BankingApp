package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.FailOperation;
import org.poo.operationTypes.TransactionOperation;
import org.poo.operationTypes.WithdrawSavingsFailOperation;
import org.poo.operationTypes.WithdrawSavingsOperation;

import java.util.List;

public class WithdrawSavingsCommand implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;
    private final ExchangeRateManager exchangeRateManager;

    public WithdrawSavingsCommand(ObjectMapper objectMapper, ArrayNode output, ExchangeRateManager exchangeRateManager) {
        this.objectMapper = objectMapper;
        this.output = output;
        this.exchangeRateManager = exchangeRateManager;
    }

    @Override
    public void execute(List<User> users, final List<Commerciant> commerciants, CommandInput command) {
        ObjectNode commandOutput = objectMapper.createObjectNode();
        commandOutput.put("command", "withdrawSavings");
        commandOutput.put("timestamp", command.getTimestamp());

        // Locate the user and the savings account
        Account savingsAccount = null;
        Account classicAccount = null;
        User currentUser = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    savingsAccount = account;
                    currentUser = user;
                    break;
                }
            }
            if (savingsAccount != null) break;
        }

        if (currentUser == null) {
            // If user not found
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("description", "Account not found");
            commandOutput.set("output", errorOutput);
            output.add(commandOutput);
            return;
        }

        if (!savingsAccount.getAccountType().equals("savings")) {
            // If account is not savings
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("description", "Account is not of type savings.");
            commandOutput.set("output", errorOutput);
            output.add(commandOutput);
            return;
        }

        // Check user's age
        if (currentUser.getAge() < 21) {
            FailOperation ageRestrictionOperation = new FailOperation(
                    command.getTimestamp(),
                    "You don't have the minimum age required."
            );
            savingsAccount.addOperation(ageRestrictionOperation);
            return;
        }

        // Find the first classic account with the specified currency
        for (Account account : currentUser.getAccounts()) {
            if (account.getAccountType().equals("classic") && account.getCurrency().equals(command.getCurrency())) {
                classicAccount = account;
                break;
            }
        }

        if (classicAccount == null) {
            FailOperation failOperation = new FailOperation(
                    command.getTimestamp(),
                    "You do not have a classic account."
            );
            savingsAccount.addOperation(failOperation);
            return;
        }

        // Calculate exchange rate
        double exchangeRate = exchangeRateManager.getExchangeRate(savingsAccount.getCurrency(), command.getCurrency());
        if (exchangeRate == -1) {
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("description", "Exchange rate not found.");
            commandOutput.set("output", errorOutput);
            output.add(commandOutput);
            return;
        }

        // Convert the amount
        double convertedAmount = command.getAmount() / exchangeRate;

        if (savingsAccount.getBalance() < convertedAmount) {
            // If insufficient funds
            ObjectNode errorOutput = objectMapper.createObjectNode();
            errorOutput.put("description", "Insufficient funds");
            commandOutput.set("output", errorOutput);
            output.add(commandOutput);
            return;
        }

        // Perform the withdrawal and deposit
        savingsAccount.removeFunds(convertedAmount);
        classicAccount.addFunds(command.getAmount());

        // Add operations to the accounts
        WithdrawSavingsOperation savingsWithdrawal = new WithdrawSavingsOperation(
                command.getTimestamp(),
                "Savings withdrawal",
                savingsAccount.getIban(),
                classicAccount.getIban(),
                convertedAmount,
                savingsAccount.getCurrency(),
                "withdrawal"
        );
        savingsAccount.addOperation(savingsWithdrawal);

        // Add operations to the accounts
        WithdrawSavingsOperation classicDeposit = new WithdrawSavingsOperation(
                command.getTimestamp(),
                "Savings withdrawal",
                savingsAccount.getIban(),
                classicAccount.getIban(),
                convertedAmount,
                savingsAccount.getCurrency(),
                "withdrawal"
        );
        classicAccount.addOperation(classicDeposit);
    }
}
