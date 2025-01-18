package org.poo.servicePlan;

public class GoldPlanDecorator extends TransactionServiceDecorator {
    public GoldPlanDecorator(TransactionService decoratedService) {
        super(decoratedService);
    }

    @Override
    public double applyCashback(double totalSpendingThreshold, double transactionAmount) {
        if (totalSpendingThreshold == 500) return transactionAmount * 0.007;  // 0.7%
        if (totalSpendingThreshold == 300) return transactionAmount * 0.0055; // 0.55%
        if (totalSpendingThreshold == 100) return transactionAmount * 0.005;  // 0.5%
        return 0.0;
    }


    @Override
    public double applyCommission(double transactionAmount) {
        return 0.0; // No commission
    }
}
