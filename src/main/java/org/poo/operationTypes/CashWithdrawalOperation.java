package org.poo.operationTypes;

import org.poo.data.Operation;

public class CashWithdrawalOperation extends Operation {
    private double amount;
    private String description;

    public CashWithdrawalOperation(final int timestamp, final double amount,
                                   final String description) {
        super(timestamp);
        this.amount = amount;
        this.description = description;
    }

    /**
     * Getter for the amount.
     * @return The withdrawn amount.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Getter for the description.
     * @return The operation description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Overrides the operation type to return "cashWithdrawal".
     * @return The type of the operation.
     */
    @Override
    public String getOperationType() {
        return "cashWithdrawal";
    }
}
