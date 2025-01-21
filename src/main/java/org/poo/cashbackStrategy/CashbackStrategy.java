package org.poo.cashbackStrategy;

public interface CashbackStrategy {
    double calculateCashback(double amount, String category,
                             int transactionCount, double totalSpending);
}
