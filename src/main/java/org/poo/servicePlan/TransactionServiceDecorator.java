package org.poo.servicePlan;

public abstract class TransactionServiceDecorator implements TransactionService {
    protected final TransactionService decoratedService;

    public TransactionServiceDecorator(TransactionService decoratedService) {
        this.decoratedService = decoratedService;
    }

    @Override
    public double applyCashback(double totalSpending, double transactionAmount) {
        return decoratedService.applyCashback(totalSpending, transactionAmount);
    }

    @Override
    public double applyCommission(double transactionAmount) {
        return decoratedService.applyCommission(transactionAmount);
    }
}
