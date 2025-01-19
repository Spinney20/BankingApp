package org.poo.operationTypes;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.data.Account;
import org.poo.data.Operation;

import java.util.Map;

/**
 * Represents a split payment operation, including details about the split type
 * and the amounts owed by each account.
 */
public class SplitPaymentOperation extends Operation {
    private double amount;
    private String currency;
    private String description;
    private ArrayNode involvedAccounts;
    private String splitPaymentType; // "equal" or "custom"
    private Map<Account, Double> splitMap; // Map of amounts per account

    public SplitPaymentOperation(final int timestamp, final double amount, final String currency,
                                 final String description, final ArrayNode involvedAccounts,
                                 final String splitPaymentType, final Map<Account, Double> splitMap) {
        super(timestamp);
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.involvedAccounts = involvedAccounts;
        this.splitPaymentType = splitPaymentType;
        this.splitMap = splitMap;
    }

    /***
     * Getter for the split payment type
     * @return - "equal" or "custom"
     */
    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    /***
     * Getter for the total amount
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
     * Getter for the description of the operation
     * @return - description as a String
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the involved accounts
     * @return - ArrayNode containing the IBANs of involved accounts
     */
    public ArrayNode getInvolvedAccounts() {
        return involvedAccounts;
    }

    /***
     * Getter for the split map (amounts owed by each account)
     * @return - A map of accounts and their respective amounts
     */
    public Map<Account, Double> getSplitMap() {
        return splitMap;
    }

    /***
     * Returns the type of the operation for identification
     * @return - "SplitPayment"
     */
    @Override
    public String getOperationType() {
        return "SplitPayment";
    }
}
