package org.poo.currencyExchange;

// this is the exchange rate that we will use to convert the currencies
// it's a simple class that holds the from, to and the rate
// this passed the input of the exchange rates to the CurrencyGraph
// and then the CurrencyGraph will calculate the exchange rate
// and then the ExchangeRateManager will get the exchange rate
// and then the Account will use the exchange rate to convert the money
public class ExchangeRate {
    private String from;
    private String to;
    private double rate;

    public ExchangeRate(final String from, final String to, final double rate) {
        this.from = from;
        this.to = to;
        this.rate = rate;
    }

    /***
     * Getter for the currency we are converting from
     * @return
     */
    public String getFrom() {
        return from;
    }

    /***
     * Getter for the currency we are converting to
     * @return
     */
    public String getTo() {
        return to;
    }

    /***
     * Getter for the exchange rate
     * @return
     */
    public double getRate() {
        return rate;
    }
}
