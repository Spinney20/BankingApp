package org.poo.servicePlan;

public class GoldPlanDecorator extends TransactionServiceDecorator {
    // Named constants instead of "magic numbers"
    private static final double THRESHOLD_500 = 500.0;
    private static final double CASHBACK_RATE_500 = 0.007; // 0.7%
    private static final double THRESHOLD_300 = 300.0;
    private static final double CASHBACK_RATE_300 = 0.0055; // 0.55%
    private static final double THRESHOLD_100 = 100.0;
    private static final double CASHBACK_RATE_100 = 0.005;  // 0.5%

    public GoldPlanDecorator(final TransactionService decoratedService) {
        super(decoratedService);
    }

    /**
     * Apply cashback based on the total spending threshold and the transaction amount.
     * @param totalSpendingThreshold
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCashback(final double totalSpendingThreshold,
                                final double transactionAmount) {
        if (totalSpendingThreshold >= THRESHOLD_500) {
            return transactionAmount * CASHBACK_RATE_500;
        }
        if (totalSpendingThreshold >= THRESHOLD_300) {
            return transactionAmount * CASHBACK_RATE_300;
        }
        if (totalSpendingThreshold >= THRESHOLD_100) {
            return transactionAmount * CASHBACK_RATE_100;
        }
        return 0.0;
    }

    /**
     * overrides base transaction service commission
     * no comission for gold plan
     * @param transactionAmount
     * @return
     */
    @Override
    public double applyCommission(final double transactionAmount) {
        return 0.0; // No commission
    }
}
