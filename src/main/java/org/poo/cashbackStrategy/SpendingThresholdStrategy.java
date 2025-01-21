package org.poo.cashbackStrategy;

import org.poo.data.User;

public class SpendingThresholdStrategy implements CashbackStrategy {

    private final User user;

    public SpendingThresholdStrategy(final User user) {
        this.user = user;
    }

    /***
     * This is the method that calculates the cashback
     * but i basically pass it to the user to apply it xD
     * @param amount - the amount of the transaction
     * @param category - the category of the transaction
     * @param transactionCount
     * @param totalSpending
     * @return
     */
    @Override
    public double calculateCashback(final double amount, final String category,
                                    final int transactionCount, final double totalSpending) {
        // Get cashback rate from the user's current plan
        return user.applyCashback(totalSpending, amount);
    }
}
