package org.poo.operationTypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.data.Operation;

import java.util.List;

/**
 * Specialized operation class for "custom" split payments.
 * Produces JSON like:
 * {
 *   "amountForUsers": [...],
 *   "currency": "...",
 *   "description": "...",
 *   "error": "...",           // only if non-null
 *   "involvedAccounts": [...],
 *   "splitPaymentType": "custom",
 *   "timestamp": ...
 * }
 */
@Getter
@Setter
public class SplitCustomPaymentOperation extends Operation {

    private double amount;                 // total split amount
    private String currency;              // e.g. "RON" or "EUR"
    private String description;           // e.g. "Split payment of 168.00 RON"
    private List<String> involvedAccounts; // IBANs of all participating accounts
    private List<Double> amountForUsers;  // amounts each IBAN pays
    private String splitPaymentType;      // "custom"
    private int timestamp;                // creation time for this split

    // We'll store an optional error message here, if there's insufficient funds
    private String error;                 // e.g. "Account ... has insufficient funds..."

    public SplitCustomPaymentOperation(int timestamp,
                                       double amount,
                                       String currency,
                                       String description,
                                       List<String> involvedAccounts,
                                       List<Double> amountForUsers,
                                       String splitPaymentType) {
        // The base Operation constructor sets the overall timestamp
        super(timestamp);
        this.timestamp = timestamp;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.involvedAccounts = involvedAccounts;
        this.amountForUsers = amountForUsers;
        this.splitPaymentType = splitPaymentType;
        this.error = null;  // no error initially
    }

    @Override
    public String getOperationType() {
        // Identifies this operation in your printTransactions switch
        return "SplitPaymentCUSTOM";
    }

    /**
     * Overriding setError(...) from the base 'Operation' class to store
     * the error message in our 'error' field. No need for instanceof checks!
     */
    @Override
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Convert this operation to JSON for printing or logs.
     * The "error" field only appears if 'error' is non-null/empty.
     */
    public ObjectNode toJson(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        // amountForUsers => array
        ArrayNode amountsArray = mapper.createArrayNode();
        for (Double value : amountForUsers) {
            amountsArray.add(value);
        }
        node.set("amountForUsers", amountsArray);

        node.put("currency", currency);
        node.put("description", description);

        // if there's an error, include it
        if (error != null && !error.isEmpty()) {
            node.put("error", error);
        }

        // involvedAccounts => array
        ArrayNode accountsArray = mapper.createArrayNode();
        for (String iban : involvedAccounts) {
            accountsArray.add(iban);
        }
        node.set("involvedAccounts", accountsArray);

        node.put("splitPaymentType", splitPaymentType);
        node.put("timestamp", timestamp);

        return node;
    }
}
