package org.poo.cashbackStrategy;

import org.poo.data.User;

public class SpendingThresholdStrategy implements CashbackStrategy {

    private final User user;

    public SpendingThresholdStrategy(User user) {
        this.user = user;
    }

    @Override
    public double calculateCashback(double amount, String category, int transactionCount, double totalSpending) {
        // Get cashback rate from the user's current plan
        return user.applyCashback(totalSpending, amount);
    }
}
