package org.poo.splitStrategy;

import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.fileio.CommandInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Equal" split: divides command.getAmount() equally
 * among all participating accounts, applying currency conversion if needed.
 */
public class EqualSplitPaymentStrategy implements SplitPaymentStrategy {

    @Override
    public Map<Account, Double> calculateSplit(final List<Account> accounts,
                                               final CommandInput command,
                                               final ExchangeRateManager exchangeRateManager) {
        double total = command.getAmount();
        String baseCurrency = command.getCurrency();
        double share = total / accounts.size();

        Map<Account, Double> result = new HashMap<>();
        for (Account acc : accounts) {
            double portion = share;
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
