package org.poo.operationTypes;

import org.poo.data.Operation;

public class FailOperation extends Operation {
    private String description;

    public FailOperation(final int timestamp, final String description) {
        super(timestamp);
        this.description = description;
    }

    /***
     * getter for the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - failure
     */
    @Override
    public String getOperationType() {
        return "failure";
    }
}
