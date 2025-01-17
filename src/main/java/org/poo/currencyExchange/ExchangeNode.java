package org.poo.currencyExchange;

// Class for the nodes in the priority queue
// i think if I made a separate file for this class it would make
// the code harder to understand
// This will be used in the currency graph as its basically the nodes of the graph
public class ExchangeNode {
    private String currency;
    private double rate;

    ExchangeNode(final String currency, final double rate) {
        this.currency = currency;
        this.rate = rate;
    }

    /***
     * Getter for the currency
     * @return - the currency as a string
     */
    public String getCurrency() {
        return currency;
    }

    /***
     * Getter for the rate
     * @return - the rate as double
     */
    public double getRate() {
        return rate;
    }
}
