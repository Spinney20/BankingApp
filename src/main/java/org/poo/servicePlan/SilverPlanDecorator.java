package org.poo.servicePlan;

public class SilverPlanDecorator extends TransactionServiceDecorator {
    public SilverPlanDecorator(TransactionService decoratedService) {
        super(decoratedService);
    }

    @Override
    public double applyCashback(double totalSpending, double transactionAmount) {
        if (totalSpending >= 500) return transactionAmount * 0.005;
        if (totalSpending >= 300) return transactionAmount * 0.004;
        if (totalSpending >= 100) return transactionAmount * 0.003;
        return 0.0;
    }

    @Override
    public double applyCommission(double transactionAmount) {
        return transactionAmount >= 500 ? transactionAmount * 0.001 : 0.0; // 0.1% commission for transactions >= 500
    }
}

