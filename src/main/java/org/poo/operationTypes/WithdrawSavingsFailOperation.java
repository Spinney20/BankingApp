package org.poo.operationTypes;

import org.poo.data.Operation;

public class WithdrawSavingsFailOperation extends Operation {
    private String description;

    public WithdrawSavingsFailOperation(int timestamp, String description) {
        super(timestamp);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getOperationType() {
        return "withdrawSavingsFail";
    }
}
