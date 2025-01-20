package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.FailOperation;
import org.poo.operationTypes.UpgradePlanOperation;

import java.util.List;

public class UpgradePlanCommand implements Command {

    private final ExchangeRateManager exchangeRateManager;
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public UpgradePlanCommand(ExchangeRateManager exchangeRateManager, ObjectMapper objectMapper, ArrayNode output) {
        this.exchangeRateManager = exchangeRateManager;
        this.objectMapper = objectMapper;
        this.output = output;
    }

    @Override
    public void execute(List<User> users, CommandInput command) {
        ObjectNode outputNode = objectMapper.createObjectNode();
        String newPlanType = command.getNewPlanType();
        String accountIban = command.getAccount();

        // Find user and account
        User upgradingUser = null;
        Account targetAccount = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(accountIban)) {
                    upgradingUser = user;
                    targetAccount = account;
                    break;
                }
            }
            if (upgradingUser != null) break;
        }

        if (upgradingUser == null) {
            outputNode.put("command", "upgradePlan");
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("description", "Account not found");
            errorNode.put("timestamp", command.getTimestamp());
            outputNode.set("output", errorNode);
            outputNode.put("timestamp", command.getTimestamp());
            output.add(outputNode);
            return;
        }

        if(targetAccount == null) {
            outputNode.put("command", "upgradePlan");
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("description", "Account not found");
            errorNode.put("timestamp", command.getTimestamp());
            outputNode.set("output", errorNode);
            outputNode.put("timestamp", command.getTimestamp());
            output.add(outputNode);
            return;
        }

        // Check for downgrade attempt or invalid plan
        String currentPlan = upgradingUser.getCurrentPlanName();

        if (currentPlan.equals(newPlanType)) {
            return;
        }

        // Determine upgrade fee in RON
        double upgradeFeeRON = determineUpgradeFee(currentPlan, newPlanType);

        // Convert fee to account currency if necessary
        double feeInAccountCurrency = upgradeFeeRON;
        if (!targetAccount.getCurrency().equalsIgnoreCase("RON")) {
            double exchangeRate = exchangeRateManager.getExchangeRate("RON", targetAccount.getCurrency());
            if (exchangeRate != -1) {
                feeInAccountCurrency = upgradeFeeRON * exchangeRate;
            } else {
                System.out.println("{\"description\": \"Exchange rate not available\", \"timestamp\": " + command.getTimestamp() + "}");
                return;
            }
        }

        // Check if the account has sufficient funds
        if (targetAccount.getBalance() < feeInAccountCurrency) {
            FailOperation failOperation = new FailOperation(
                    command.getTimestamp(),
                    "Insufficient funds"
            );
            targetAccount.addOperation(failOperation);
            return;
        }

        // Deduct fee and complete the upgrade
        upgradingUser.upgradePlan(newPlanType);
        targetAccount.removeFunds(feeInAccountCurrency);

        // Add upgrade operation to the account
        UpgradePlanOperation upgradeOperation = new UpgradePlanOperation(
                command.getTimestamp(),
                accountIban,
                newPlanType,
                targetAccount.getCurrency()
        );
        targetAccount.addOperation(upgradeOperation);
    }

    /**
     * Determines the upgrade fee based on the user's current plan and the new plan.
     *
     * @param currentPlan The current plan name.
     * @param newPlanType The new plan name.
     * @return The upgrade fee in RON.
     */
    private double determineUpgradeFee(String currentPlan, String newPlanType) {
        // Normalize plan names
        String normalizedCurrentPlan = currentPlan.replace("PlanDecorator", "").toLowerCase();
        String normalizedNewPlanType = newPlanType.toLowerCase();

        if ((normalizedCurrentPlan.equals("standard") || normalizedCurrentPlan.equals("student"))
                && normalizedNewPlanType.equals("silver")) {
            return 100.0;
        } else if (normalizedCurrentPlan.equals("silver") && normalizedNewPlanType.equals("gold")) {
            return 250.0;
        } else if ((normalizedCurrentPlan.equals("standard") || normalizedCurrentPlan.equals("student"))
                && normalizedNewPlanType.equals("gold")) {
            return 350.0;
        }

        return 0.0;
    }
}
