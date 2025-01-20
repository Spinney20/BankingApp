package org.poo.operationTypes;

import org.poo.data.Operation;

public class WithdrawSavingsOperation extends Operation {
    private String senderIBAN;
    private String receiverIBAN;
    private double amount;
    private String currency;
    private String description;
    private String transferType; // sent / received

    public WithdrawSavingsOperation(final int timestamp, final String description,
                                final String senderIBAN, final String receiverIBAN,
                                final double amount, final String currency,
                                final String transferType) {
        super(timestamp);
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.transferType = transferType;
    }

    /***
     * Getter for the sender IBAN
     * @return - the iban as a string
     */
    public String getSenderIBAN() {
        return senderIBAN;
    }

    /***
     * Getter for the receiver IBAN
     * @return - the iban as a string
     */
    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    /***
     * Getter for the amount
     * @return
     */
    public double getAmount() {
        return amount;
    }

    /***
     * Getter for the currency
     * @return
     */
    public String getCurrency() {
        return currency;
    }

    /***
     * Getter for the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the transfer type
     * @return - its a string
     */
    public String getTransferType() {
        return transferType;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - transaction
     */
    @Override
    public String getOperationType() {
        return "withdrawSav";
    }
}
