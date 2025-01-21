package org.poo.servicePlan;

public interface TransactionService {
    double applyCashback(double totalSpending, double transactionAmount);
    double applyCommission(double transactionAmount);
}
