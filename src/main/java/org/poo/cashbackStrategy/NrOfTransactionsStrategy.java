package org.poo.cashbackStrategy;

import org.poo.data.Account;

public class NrOfTransactionsStrategy implements CashbackStrategy {
    private final Account account;

    public NrOfTransactionsStrategy(final Account account) {
        this.account = account;
    }

    @Override
    public double calculateCashback(final double amount, final String category,
                                    final int transactionCount, final double totalSpending) {
        if (category.equals("Food") && transactionCount >= 2
                && !account.hasUsedNrOfTransactionsCashback("Food")) {
            account.markNrOfTransactionsCashbackAsUsed("Food");
            return amount * 0.01;
        }
        if (category.equals("Clothes") && transactionCount >= 5
                && !account.hasUsedNrOfTransactionsCashback("Clothes")) {
            account.markNrOfTransactionsCashbackAsUsed("Clothes");
            return amount * 0.05;
        }
        if (category.equals("Tech") && transactionCount >= 10
                && !account.hasUsedNrOfTransactionsCashback("Tech")) {
            account.markNrOfTransactionsCashbackAsUsed("Tech");
            return amount * 0.10;
        }
        return 0.0;
    }
}
