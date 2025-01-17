package org.poo.currencyExchange;

import java.util.List;

public class ExchangeRateManager {
    private CurrencyGraph currencyGraph;

    // constructor
    // creates a new CurrencyGraph and adds the exchange rates to it
    public ExchangeRateManager(final List<ExchangeRate> exchangeRates) {
        this.currencyGraph = new CurrencyGraph();
        for (ExchangeRate rate : exchangeRates) {
            currencyGraph.addExchangeRate(rate.getFrom(), rate.getTo(), rate.getRate());
            currencyGraph.addExchangeRate(rate.getTo(), rate.getFrom(), 1.0 / rate.getRate());
        }
    }

    /***
     * getter for the exchange rate of the two currencies
     * given from the graph
     * basically getter of a getter
     * @param fromCurrency - the currency we are converting from
     * @param toCurrency - the currency we are converting to
     * @return - the exchange rate desired
     */
    public double getExchangeRate(final String fromCurrency, final String toCurrency) {
        return currencyGraph.getExchangeRate(fromCurrency, toCurrency);
    }
}
