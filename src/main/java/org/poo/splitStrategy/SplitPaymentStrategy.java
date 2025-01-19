package org.poo.splitStrategy;

import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.fileio.CommandInput;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for calculating the distribution of amounts
 * among the participating accounts.
 */
public interface SplitPaymentStrategy {
    Map<Account, Double> calculateSplit(
            List<Account> accounts,
            CommandInput command,
            ExchangeRateManager exchangeRateManager
    );
}
