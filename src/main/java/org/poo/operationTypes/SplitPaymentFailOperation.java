package org.poo.operationTypes;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.data.Operation;

public class SplitPaymentFailOperation extends Operation {
    private double amount;
    private String description;
    private String error;
    private ArrayNode involvedAccounts;
    private String currency;
    private String splitPaymentType; // "equal" or "custom"

    public SplitPaymentFailOperation(final int timestamp, final double amount, final String currency,
                                     final String description, final String error,
                                     final ArrayNode involvedAccounts, final String splitPaymentType) {
        super(timestamp);
        this.amount = amount;
        this.description = description;
        this.error = error;
        this.involvedAccounts = involvedAccounts;
        this.currency = currency;
        this.splitPaymentType = splitPaymentType;
    }

    /***
     * Getter for the error description
     * @return - error message as a String
     */
    public String getError() {
        return error;
    }

    /***
     * Getter for the operation description
     * @return - description as a String
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the accounts involved in the split
     * @return - ArrayNode containing the IBANs of involved accounts
     */
    public ArrayNode getInvolvedAccounts() {
        return involvedAccounts;
    }

    /***
     * Getter for the total amount associated with the split payment
     * @return - the total amount as a double
     */
    public double getAmount() {
        return amount;
    }

    /***
     * Getter for the currency used in the transaction
     * @return - currency as a String
     */
    public String getCurrency() {
        return currency;
    }

    /***
     * Getter for the split payment type
     * @return - "equal" or "custom"
     */
    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    /***
     * Returns the type of the operation for identification
     * @return - "SplitPaymentFail"
     */
    @Override
    public String getOperationType() {
        return "SplitPaymentFail";
    }
}
