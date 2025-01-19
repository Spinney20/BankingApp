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
 * It produces JSON in the format:
 *
 * {
 *   "amountForUsers": [ 90, 78 ],
 *   "currency": "RON",
 *   "description": "Split payment of 168.00 RON",
 *   "involvedAccounts": [ "RO69...", "RO58..." ],
 *   "splitPaymentType": "custom",
 *   "timestamp": 5
 * }
 */
@Getter
@Setter
public class SplitCustomPaymentOperation extends Operation {

    private double amount;                // total amount
    private String currency;             // e.g. "RON"
    private String description;          // e.g. "Split payment of 168.00 RON"
    private List<String> involvedAccounts; // IBANs of the involved accounts
    private List<Double> amountForUsers; // amounts for each IBAN in the same order
    private String splitPaymentType;     // always "custom"
    private int timestamp;               // creation timestamp of the splitPayment command

    public SplitCustomPaymentOperation(int timestamp,
                                       double amount,
                                       String currency,
                                       String description,
                                       List<String> involvedAccounts,
                                       List<Double> amountForUsers,
                                       String splitPaymentType) {
        // Call the superclass constructor if 'Operation' requires a timestamp
        super(timestamp);
        this.timestamp = timestamp;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.involvedAccounts = involvedAccounts;
        this.amountForUsers = amountForUsers;
        this.splitPaymentType = splitPaymentType;
    }

    @Override
    public String getOperationType() {
        return "SplitPaymentCUSTOM";
    }

    /**
     * If you have a printing or JSON-building mechanism,
     * you can override a method (e.g. toJson) to produce the exact layout.
     * Adjust to match however your system prints operations.
     */
    public ObjectNode toJson(ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        // amountForUsers
        ArrayNode amountsArray = mapper.createArrayNode();
        for (Double value : amountForUsers) {
            amountsArray.add(value);
        }
        node.set("amountForUsers", amountsArray);

        // currency
        node.put("currency", currency);

        // description
        node.put("description", description);

        // involvedAccounts
        ArrayNode accountsArray = mapper.createArrayNode();
        for (String iban : involvedAccounts) {
            accountsArray.add(iban);
        }
        node.set("involvedAccounts", accountsArray);

        // splitPaymentType
        node.put("splitPaymentType", splitPaymentType);

        // timestamp
        node.put("timestamp", timestamp);

        return node;
    }
}
