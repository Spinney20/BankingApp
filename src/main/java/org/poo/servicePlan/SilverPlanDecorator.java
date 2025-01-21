package org.poo.servicePlan;

public class SilverPlanDecorator extends TransactionServiceDecorator {

    // Named constants for thresholds and rates
    private static final double THRESHOLD_500 = 500.0;
    private static final double CASHBACK_RATE_500 = 0.005;    // 0.5%
    private static final double THRESHOLD_300 = 300.0;
    private static final double CASHBACK_RATE_300 = 0.004;    // 0.4%
    private static final double THRESHOLD_100 = 100.0;
    private static final double CASHBACK_RATE_100 = 0.003;    // 0.3%

    private static final double COMMISSION_THRESHOLD = 500.0;
    private static final double COMMISSION_RATE = 0.001;       // 0.1%

    public SilverPlanDecorator(final TransactionService decoratedService) {
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
            return transactionAmount * CASHBACK_RATE_500;  // 0.5%
        }
        if (totalSpendingThreshold >= THRESHOLD_300) {
            return transactionAmount * CASHBACK_RATE_300;  // 0.4%
        }
        if (totalSpendingThreshold >= THRESHOLD_100) {
            return transactionAmount * CASHBACK_RATE_100;  // 0.3%
        }
        return 0.0;
    }

    /***
     * applying comission for silver plan
     * 0.1% comission for transaction amount >= 500
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCommission(final double transactionAmount) {
        return (transactionAmount >= COMMISSION_THRESHOLD)
                ? (transactionAmount * COMMISSION_RATE)
                : 0.0;
    }
}
