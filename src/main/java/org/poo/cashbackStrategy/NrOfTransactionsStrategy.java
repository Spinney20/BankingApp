package org.poo.cashbackStrategy;

import org.poo.data.Account;

public class NrOfTransactionsStrategy implements CashbackStrategy {

    // Named constants instead of "magic numbers"
    private static final int FOOD_THRESHOLD = 2;
    private static final double FOOD_CASHBACK_RATE = 0.01;

    private static final int CLOTHES_THRESHOLD = 5;
    private static final double CLOTHES_CASHBACK_RATE = 0.05;

    private static final int TECH_THRESHOLD = 10;
    private static final double TECH_CASHBACK_RATE = 0.10;

    private final Account account;

    public NrOfTransactionsStrategy(final Account account) {
        this.account = account;
    }

    /***
     * This is the method that calculates the cashback
     * for the number of transactions strategy
     * basically, if the number of transactions is greater than
     * or equal to a certain number, and the account has not used
     * the cashback for that category, it will return the cashback
     * for that category
     * @param amount - the amount of the transaction
     * @param category - the category of the transaction
     * @param transactionCount - the number of transactions
     * @param totalSpending - the total spending of the account on ...
     * @return - the cashback amount
     */
    @Override
    public double calculateCashback(final double amount,
                                    final String category,
                                    final int transactionCount,
                                    final double totalSpending) {

        if (category.equals("Food")
                && transactionCount >= FOOD_THRESHOLD
                && !account.hasUsedNrOfTransactionsCashback("Food")) {
            account.markNrOfTransactionsCashbackAsUsed("Food");
            return amount * FOOD_CASHBACK_RATE;
        }

        if (category.equals("Clothes")
                && transactionCount >= CLOTHES_THRESHOLD
                && !account.hasUsedNrOfTransactionsCashback("Clothes")) {
            account.markNrOfTransactionsCashbackAsUsed("Clothes");
            return amount * CLOTHES_CASHBACK_RATE;
        }

        if (category.equals("Tech")
                && transactionCount >= TECH_THRESHOLD
                && !account.hasUsedNrOfTransactionsCashback("Tech")) {
            account.markNrOfTransactionsCashbackAsUsed("Tech");
            return amount * TECH_CASHBACK_RATE;
        }

        return 0.0;
    }
}
