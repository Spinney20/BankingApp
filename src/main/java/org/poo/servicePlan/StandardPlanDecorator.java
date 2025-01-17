package org.poo.servicePlan;

public class StandardPlanDecorator extends TransactionServiceDecorator {
    public StandardPlanDecorator(TransactionService decoratedService) {
        super(decoratedService);
    }

    @Override
    public double applyCashback(double totalSpending, double transactionAmount) {
        if (totalSpending >= 500) return transactionAmount * 0.0025;
        if (totalSpending >= 300) return transactionAmount * 0.002;
        if (totalSpending >= 100) return transactionAmount * 0.001;
        return 0.0;
    }

    @Override
    public double applyCommission(double transactionAmount) {
        return transactionAmount * 0.002; // 0.2% commission
    }
}

