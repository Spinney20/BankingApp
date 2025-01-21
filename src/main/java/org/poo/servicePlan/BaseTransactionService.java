package org.poo.servicePlan;

public class BaseTransactionService implements TransactionService {
    /***
     * Applies cashback to the transaction amount.
     * no cashback by default
     * @param totalSpending
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCashback(final double totalSpending, final double transactionAmount) {
        return 0.0; // No cashback by default
    }

    /***
     * Applies commission to the transaction amount.
     * no commission by default
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCommission(final double transactionAmount) {
        return 0.0; // No commission by default
    }
}
