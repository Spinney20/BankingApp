package org.poo.operationTypes;

import org.poo.data.Operation;

public class CreateCardOperation extends Operation {
    private String accountIBAN;
    private String cardNumber;
    private String cardHolder;
    private String description;

    public CreateCardOperation(final int timestamp, final String accountIBAN,
                               final String cardNumber, final String cardHolder,
                               final String description) {
        super(timestamp);
        this.accountIBAN = accountIBAN;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.description = description;
    }

    /***
     * Getter for the iban
     * @return - the account's iban as a string
     */
    public String getAccountIBAN() {
        return accountIBAN;
    }

    /***
     * Getter for the card nr
     * @return - the card nr as a string
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /***
     * Getter for the card Holder
     * @return
     */
    public String getCardHolder() {
        return cardHolder;
    }

    /***
     * Getter for description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - cardCreation
     */
    @Override
    public String getOperationType() {
        return "cardCreation";
    }
}
