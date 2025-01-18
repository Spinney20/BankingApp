package org.poo.servicePlan;

public class StandardPlanDecorator extends TransactionServiceDecorator {
    public StandardPlanDecorator(TransactionService decoratedService) {
        super(decoratedService);
    }

    @Override
    public double applyCashback(double totalSpendingThreshold, double transactionAmount) {
        if (totalSpendingThreshold == 500) return transactionAmount * 0.0025; // 0.25%
        if (totalSpendingThreshold == 300) return transactionAmount * 0.002;  // 0.2%
        if (totalSpendingThreshold == 100) return transactionAmount * 0.001;  // 0.1%
        return 0.0;
    }


    @Override
    public double applyCommission(double transactionAmount) {
        return transactionAmount * 0.002; // 0.2% commission
    }
}

