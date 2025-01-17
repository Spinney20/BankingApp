package org.poo.operationTypes;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.data.Operation;


public class SplitPaymentFailOperation extends Operation {
    private double amount;
    private String description;
    private String error;
    private ArrayNode involvedAccounts;
    private String currency;

    public SplitPaymentFailOperation(final int timestamp, final String description,
                                     final String error, final ArrayNode involvedAccounts,
                                     final double amount, final String currency) {
        super(timestamp);
        this.amount = amount;
        this.description = description;
        this.error = error;
        this.involvedAccounts = involvedAccounts;
        this.currency = currency;
    }

    /***
     * getter for the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /***
     * getter for the accounts involved in the split
     * @return - arrayNode of the involved accounts
     */
    public ArrayNode getInvolvedAccounts() {
        return involvedAccounts;
    }

    /***
     * Getter for the amount paid
     * @return - the amount as double
     */
    public double getAmount() {
        return amount;
    }

    /***
     * The currency of the amount
     * @return
     */
    public String getCurrency() {
        return currency;
    }

    /***
     * Getter for the error cause its a fail
     * @return
     */
    public String getError() {
        return error;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - splitFailure
     */
    @Override
    public String getOperationType() {
        return "splitFailure";
    }
}

