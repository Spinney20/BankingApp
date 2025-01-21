package org.poo.data;

public abstract class Operation {
    private int timestamp;

    public Operation(final int timestamp) {
        this.timestamp = timestamp;
    }

    /***
     * getter for the timestamp
     * used for each output and also sorting the operations by timestamp
     * @return - the timestamp when the operation has been done
     */
    public int getTimestamp() {
        return timestamp;
    }

    /***
     * Getter for the operation type
     * @return - i have multiple operations and I have a string to identify each one
     * as you will see.
     */
    public abstract String getOperationType();

    /***
     * Setter for the error message
     * @param error - the error message
     */
    public void setError(final String error) {
        // By default, do nothing
        // Subclasses that need error functionality will override this
    }

}

