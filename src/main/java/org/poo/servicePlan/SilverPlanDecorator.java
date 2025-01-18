package org.poo.servicePlan;

public class SilverPlanDecorator extends TransactionServiceDecorator {
    public SilverPlanDecorator(TransactionService decoratedService) {
        super(decoratedService);
    }

    @Override
    public double applyCashback(double totalSpendingThreshold, double transactionAmount) {
        if (totalSpendingThreshold == 500) return transactionAmount * 0.005;  // 0.5%
        if (totalSpendingThreshold == 300) return transactionAmount * 0.004;  // 0.4%
        if (totalSpendingThreshold == 100) return transactionAmount * 0.003;  // 0.3%
        return 0.0;
    }

    @Override
    public double applyCommission(double transactionAmount) {
        return transactionAmount >= 500 ? transactionAmount * 0.001 : 0.0; // 0.1% commission for transactions >= 500
    }
}

