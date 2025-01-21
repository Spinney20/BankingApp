package org.poo.servicePlan;

public class BaseTransactionService implements TransactionService {
    @Override
    public double applyCashback(final double totalSpending, final double transactionAmount) {
        return 0.0; // No cashback by default
    }

    @Override
    public double applyCommission(final double transactionAmount) {
        return 0.0; // No commission by default
    }
}
