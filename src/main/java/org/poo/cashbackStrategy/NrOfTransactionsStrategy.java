package org.poo.cashbackStrategy;

public class NrOfTransactionsStrategy implements CashbackStrategy {
    @Override
    public double calculateCashback(double amount, String category, int transactionCount, double totalSpending) {
        if (category.equals("Food") && transactionCount >= 2) {
            return amount * 0.02;
        }
        if (category.equals("Clothes") && transactionCount >= 5) {
            return amount * 0.05;
        }
        if (category.equals("Tech") && transactionCount >= 10) {
            return amount * 0.10;
        }
        return 0.0;
    }
}
