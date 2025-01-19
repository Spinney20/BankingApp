package org.poo.operationTypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    @Override
    public String getOperationType() {
        return "SplitPaymentEQUAL";
    }

    @Override
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Build a JSON object for printing or logging.
     */
    public ObjectNode toJson(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        // amount (the total for the entire equal split)
        node.put("amount", amount);

        node.put("currency", currency);
        node.put("description", description);

        // error if present
        if (error != null && !error.isEmpty()) {
            node.put("error", error);
        }

        // involvedAccounts (already an ArrayNode of strings)
        node.set("involvedAccounts", involvedAccounts);

        node.put("splitPaymentType", splitPaymentType);
        node.put("timestamp", getTimestamp());

        return node;
    }
}
