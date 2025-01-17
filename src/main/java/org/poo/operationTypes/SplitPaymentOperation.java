package org.poo.operationTypes;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.data.Operation;

public class  SplitPaymentOperation extends Operation {
    private double amount;
    private String currency;
    private String description;
    private ArrayNode involvedAccounts;

    public SplitPaymentOperation(final int timestamp, final double amount, final String currency,
                                 final String description, final ArrayNode involvedAccounts) {
        super(timestamp);
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.involvedAccounts = involvedAccounts;
    }

    /***
     * Getter for the amount
     * @return
     */
    public double getAmount() {
        return amount;
    }

    /***
     * Getter for the currency
     * @return
     */
    public String getCurrency() {
        return currency;
    }

    /***
     * getter for the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the involved accounts
     * @return - array node of the involved accounts
     */
    public ArrayNode getInvolvedAccounts() {
        return involvedAccounts;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - SplitPayment
     */
    @Override
    public String getOperationType() {
        return "SplitPayment";
    }
}
