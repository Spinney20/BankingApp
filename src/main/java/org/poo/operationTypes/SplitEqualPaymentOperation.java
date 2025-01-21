package org.poo.operationTypes;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.data.Account;
import org.poo.data.Operation;

import java.util.Map;

/**
 * Represents an "equal" split payment operation.
 *
 * Example JSON might be:
 * {
 *   "amount": 2235,
 *   "currency": "EUR",
 *   "description": "Split payment of 2235.00 EUR",
 *   "error": "...",          // only if non-null
 *   "involvedAccounts": [...],
 *   "splitPaymentType": "equal",
 *   "timestamp": 28
 * }
 */
@Getter
@Setter
public class SplitEqualPaymentOperation extends Operation {
    private double amount;
    private String currency;
    private String description;
    private ArrayNode involvedAccounts;  // We'll store an ArrayNode of IBANs
    private String splitPaymentType;
    private Map<Account, Double> splitMap;

    // Optional error, if funds are insufficient
    private String error;

    public SplitEqualPaymentOperation(final int timestamp,
                                      final double amount,
                                      final String currency,
                                      final String description,
                                      final ArrayNode involvedAccounts,
                                      final String splitPaymentType,
                                      final Map<Account, Double> splitMap) {
        super(timestamp);
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.involvedAccounts = involvedAccounts;
        this.splitPaymentType = splitPaymentType;
        this.splitMap = splitMap;
        this.error = null;
    }

    /***
     * i have multiple operations and I have a string to identify each one
     * @return
     */
    @Override
    public String getOperationType() {
        return "SplitPaymentEQUAL";
    }

    /***
     * Setter for the error message
     * @param error - the error message
     */
    @Override
    public void setError(final String error) {
        this.error = error;
    }
}
