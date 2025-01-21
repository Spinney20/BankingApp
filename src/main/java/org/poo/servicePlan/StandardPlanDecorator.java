package org.poo.servicePlan;

public class StandardPlanDecorator extends TransactionServiceDecorator {

    // Named constants
    private static final double THRESHOLD_500 = 500.0;
    private static final double CASHBACK_RATE_500 = 0.0025; // 0.25%
    private static final double THRESHOLD_300 = 300.0;
    private static final double CASHBACK_RATE_300 = 0.002;  // 0.2%
    private static final double THRESHOLD_100 = 100.0;
    private static final double CASHBACK_RATE_100 = 0.001;  // 0.1%

    private static final double COMMISSION_RATE = 0.002;    // 0.2%

    public StandardPlanDecorator(final TransactionService decoratedService) {
        super(decoratedService);
    }

    /***
     * Apply cashback based on the total spending threshold and the transaction amount.
     * @param totalSpendingThreshold
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCashback(final double totalSpendingThreshold,
                                final double transactionAmount) {
        if (totalSpendingThreshold >= THRESHOLD_500) {
            return transactionAmount * CASHBACK_RATE_500; // 0.25%
        }
        if (totalSpendingThreshold >= THRESHOLD_300) {
            return transactionAmount * CASHBACK_RATE_300; // 0.2%
        }
        if (totalSpendingThreshold >= THRESHOLD_100) {
            return transactionAmount * CASHBACK_RATE_100; // 0.1%
        }
        return 0.0;
    }

    /***
     * applying comission for standard plan
     * 0.2% comission for transaction amount
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCommission(final double transactionAmount) {
        return transactionAmount * COMMISSION_RATE; // 0.2% commission
    }
}
