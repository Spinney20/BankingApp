package org.poo.operationTypes;

import org.poo.data.Operation;

public class DeleteCardOperation extends Operation {
    private String accountIBAN;
    private String cardNumber;
    private String cardHolder;
    private String description;

    public DeleteCardOperation(final int timestamp, final String accountIBAN,
                               final String cardNumber, final String cardHolder,
                               final String description) {
        super(timestamp);
        this.accountIBAN = accountIBAN;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.description = description;
    }

    /***
     * Getter for the aaccount iban
     * @return - the iban of the account as a string
     */
    public String getAccount() {
        return accountIBAN;
    }

    /***
     * Getter for the card number
     * @return - hte card number as a string
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /***
     * Getter for the card holder
     * @return
     */
    public String getCardHolder() {
        return cardHolder;
    }

    /***
     * Getter for the description
     * @return
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - deleteCard
     */
    @Override
    public String getOperationType() {
        return "deleteCard";
    }
}
