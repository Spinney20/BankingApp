package org.poo.operationTypes;

import org.poo.data.Operation;

public class InfoOperation extends Operation {
    private String description;

    public InfoOperation(final int timestamp, final String description) {
        super(timestamp);
        this.description = description;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - info
     */
    @Override
    public String getOperationType() {
        return "info";
    }

    /***
     * Getter for description
     * @return
     */
    public String getDescription() {
        return description;
    }
}
