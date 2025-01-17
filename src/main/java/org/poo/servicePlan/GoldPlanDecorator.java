package org.poo.servicePlan;

public class GoldPlanDecorator extends TransactionServiceDecorator {
    public GoldPlanDecorator(TransactionService decoratedService) {
        super(decoratedService);
    }

    @Override
    public double applyCashback(double totalSpending, double transactionAmount) {
        if (totalSpending >= 500) return transactionAmount * 0.007;
        if (totalSpending >= 300) return transactionAmount * 0.0055;
        if (totalSpending >= 100) return transactionAmount * 0.005;
        return 0.0;
    }

    @Override
    public double applyCommission(double transactionAmount) {
        return 0.0; // No commission
    }
}
