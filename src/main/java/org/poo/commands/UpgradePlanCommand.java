package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.UpgradePlanOperation;

import java.util.List;

public class UpgradePlanCommand implements Command {

    private final ExchangeRateManager exchangeRateManager;

    public UpgradePlanCommand(ExchangeRateManager exchangeRateManager) {
        this.exchangeRateManager = exchangeRateManager;
    }

    @Override
    public void execute(List<User> users, CommandInput command) {
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
            System.out.println("{\"description\": \"Account not found\", \"timestamp\": " + command.getTimestamp() + "}");
            return;
        }

        // Check for downgrade attempt or invalid plan
        String currentPlan = upgradingUser.getCurrentPlanName();
        String upgradeResult = upgradingUser.upgradePlan(newPlanType);

        if (upgradeResult.startsWith("Invalid") || upgradeResult.startsWith("The user already")) {
            System.out.println("{\"description\": \"" + upgradeResult + "\", \"timestamp\": " + command.getTimestamp() + "}");
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
            System.out.println("{\"description\": \"Insufficient funds\", \"timestamp\": " + command.getTimestamp() + "}");
            return;
        }

        // Deduct fee and complete the upgrade
        targetAccount.removeFunds(feeInAccountCurrency);

        // Add upgrade operation to the account
        UpgradePlanOperation upgradeOperation = new UpgradePlanOperation(
                command.getTimestamp(),
                accountIban,
                newPlanType,
                targetAccount.getCurrency()
        );
        targetAccount.addOperation(upgradeOperation);

        // Log success
        System.out.println("{\"description\": \"Upgrade plan\", \"timestamp\": " + command.getTimestamp() + "}");
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
