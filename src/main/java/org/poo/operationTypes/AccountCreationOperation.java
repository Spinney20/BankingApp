package org.poo.operationTypes;

import org.poo.data.Operation;

public class AccountCreationOperation extends Operation {
    private String description;

    public AccountCreationOperation(final int timestamp, final String description) {
        super(timestamp);
        this.description = description;
    }

    /***
     * Getter for the description
     * @return - description as a string
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - accountCreation
     */
    @Override
    public String getOperationType() {
        return "accountCreation";
    }

}

