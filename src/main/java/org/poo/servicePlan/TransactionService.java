package org.poo.servicePlan;

public interface TransactionService {
    /***
     * Apply cashback based on the total spending and the transaction amount.
     * @param totalSpending
     * @param transactionAmount
     * @return
     */
    double applyCashback(double totalSpending, double transactionAmount);

    /***
     * Apply commission based on the transaction amount.
     * @param transactionAmount
     * @return
     */
    double applyCommission(double transactionAmount);
}
