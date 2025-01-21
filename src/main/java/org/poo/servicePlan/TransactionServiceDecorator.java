package org.poo.servicePlan;

public abstract class TransactionServiceDecorator implements TransactionService {
    protected final TransactionService decoratedService;

    public TransactionServiceDecorator(final TransactionService decoratedService) {
        this.decoratedService = decoratedService;
    }

    /***
     * Apply cashback based on the total spending and the transaction amount.
     * @param totalSpending
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCashback(final double totalSpending, final double transactionAmount) {
        return decoratedService.applyCashback(totalSpending, transactionAmount);
    }

    /***
     * Apply commission based on the transaction amount.
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCommission(final double transactionAmount) {
        return decoratedService.applyCommission(transactionAmount);
    }
}
