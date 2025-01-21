package org.poo.cashbackStrategy;

/***
 * This is the interface for the cashback strategy
 * basically implements a method to know how much cashback
 */
public interface CashbackStrategy {
    /***
     * THE STRATEGY FOR THE CASHBACK
     * @param amount
     * @param category
     * @param transactionCount
     * @param totalSpending
     * @return
     */
    double calculateCashback(double amount, String category,
                             int transactionCount, double totalSpending);
}
