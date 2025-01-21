package org.poo.operationTypes;

import org.poo.data.Operation;

public class WithdrawSavingsFailOperation extends Operation {
    private String description;

    public WithdrawSavingsFailOperation(final int timestamp, final String description) {
        super(timestamp);
        this.description = description;
    }

    /***
     * Getter for the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /***
     * i have multiple operations and I have a string to identify each one
     * @return
     */
    @Override
    public String getOperationType() {
        return "withdrawSavingsFail";
    }
}
