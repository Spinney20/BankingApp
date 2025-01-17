package org.poo.cashbackStrategy;

public class SpendingThresholdStrategy implements CashbackStrategy {
    @Override
    public double calculateCashback(double amount, String category, int transactionCount, double totalSpending) {
        if (totalSpending >= 500) {
            return amount * 0.005;
        }
        if (totalSpending >= 300) {
            return amount * 0.004;
        }
        if (totalSpending >= 100) {
            return amount * 0.001;
        }
        return 0.0;
    }
}
