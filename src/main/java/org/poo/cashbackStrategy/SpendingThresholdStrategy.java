package org.poo.cashbackStrategy;

import org.poo.data.User;

public class SpendingThresholdStrategy implements CashbackStrategy {

    private final User user;

    public SpendingThresholdStrategy(final User user) {
        this.user = user;
    }

    @Override
    public double calculateCashback(final double amount, final String category,
                                    final int transactionCount, final double totalSpending) {
        // Get cashback rate from the user's current plan
        return user.applyCashback(totalSpending, amount);
    }
}
