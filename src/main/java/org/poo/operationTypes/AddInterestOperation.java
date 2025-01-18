package org.poo.operationTypes;

import org.poo.data.Operation;

public class AddInterestOperation extends Operation {
    private double amount;
    private String currency;
    private String description;

    public AddInterestOperation(final int timestamp, final double amount, final String currency) {
        super(timestamp);
        this.amount = amount;
        this.currency = currency;
        this.description = "Interest rate income";
    }

    /***
     * Getter pentru suma dobânzii
     * @return - dobânda
     */
    public double getAmount() {
        return amount;
    }

    /***
     * Getter pentru moneda contului
     * @return - moneda
     */
    public String getCurrency() {
        return currency;
    }

    /***
     * Getter pentru descriere
     * @return - descrierea operațiunii
     */
    public String getDescription() {
        return description;
    }

    /***
     * Tipul operațiunii
     * @return - tipul "addInterest"
     */
    @Override
    public String getOperationType() {
        return "addInterest";
    }
}
