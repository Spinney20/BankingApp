package org.poo.splitStrategy;

import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.fileio.CommandInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Custom" split: uses command.getAmountForUsers() to determine
 * how much each account should pay, applying currency conversion if needed.
 */
public class CustomSplitPaymentStrategy implements SplitPaymentStrategy {

    /***
     * Calculate the split for a custom payment.
     * The amounts are taken from the command.getAmountForUsers() list.
     * @param accounts
     * @param command
     * @param exchangeRateManager
     * @return
     */
    @Override
    public Map<Account, Double> calculateSplit(final List<Account> accounts,
                                               final CommandInput command,
                                               final ExchangeRateManager exchangeRateManager) {
        List<Double> amounts = command.getAmountForUsers();
        if (amounts == null || amounts.size() != accounts.size()) {
            throw new IllegalArgumentException("Mismatch between accounts and custom amounts");
        }
        String baseCurrency = command.getCurrency();

        Map<Account, Double> result = new HashMap<>();
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            double portion = amounts.get(i);

            // Convert if the account's currency differs
            if (!acc.getCurrency().equals(baseCurrency)) {
                double rate = exchangeRateManager.getExchangeRate(baseCurrency, acc.getCurrency());
                if (rate < 0) {
                    throw new RuntimeException("No exchange rate for " + acc.getCurrency());
                }
                portion *= rate;
            }
            result.put(acc, portion);
        }
        return result;
    }
}
