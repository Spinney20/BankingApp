package org.poo.servicePlan;

public class StudentPlanDecorator extends TransactionServiceDecorator {
    public StudentPlanDecorator(TransactionService decoratedService) {
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
        return 0.0; // No commission
    }
}

