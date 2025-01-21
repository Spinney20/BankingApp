package org.poo.servicePlan;

public class StudentPlanDecorator extends TransactionServiceDecorator {

    // Named constants
    private static final double THRESHOLD_500 = 500.0;
    private static final double CASHBACK_RATE_500 = 0.0025;
    private static final double THRESHOLD_300 = 300.0;
    private static final double CASHBACK_RATE_300 = 0.002;
    private static final double THRESHOLD_100 = 100.0;
    private static final double CASHBACK_RATE_100 = 0.001;

    public StudentPlanDecorator(final TransactionService decoratedService) {
        super(decoratedService);
    }

    /***
     * Apply cashback based on the total spending and the transaction amount.
     * @param totalSpending
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCashback(final double totalSpending,
                                final double transactionAmount) {
        if (totalSpending >= THRESHOLD_500) {
            return transactionAmount * CASHBACK_RATE_500;
        }
        if (totalSpending >= THRESHOLD_300) {
            return transactionAmount * CASHBACK_RATE_300;
        }
        if (totalSpending >= THRESHOLD_100) {
            return transactionAmount * CASHBACK_RATE_100;
        }
        return 0.0;
    }

    /***
     * no comission for student plan
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCommission(final double transactionAmount) {
        return 0.0; // No commission
    }
}
