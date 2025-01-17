package org.poo.operationTypes;

import org.poo.data.Operation;

public class CardPaymentOperation extends Operation {
    private String accountIBAN;
    private String cardNumber;
    private String commerciant;
    private double amount;
    private String currency;
    private String description;

    public CardPaymentOperation(final int timestamp, final String accountIBAN,
                                final String cardNumber, final String commerciant,
                                final double amount, final String currency,
                                final String description) {
        super(timestamp);
        this.accountIBAN = accountIBAN;
        this.cardNumber = cardNumber;
        this.commerciant = commerciant;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    /***
     * Getter for the account
     * @return - the iban of the account
     */
    public String getAccount() {
        return accountIBAN;
    }

    /***
     * Getter for the card number
     * @return - the card nr as a string
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /***
     * Getter for the name of the commerciant
     * @return - the name of commerciant as a string
     */
    public String getCommerciant() {
        return commerciant;
    }

    /***
     * Getter for the amount
     * @return - the amount paid
     */
    public double getAmount() {
        return amount;
    }

    /***
     * getter for the currency
     * @return - the currency in which the operation was done
     */
    public String getCurrency() {
        return currency;
    }

    /***
     * getter for the description
     * @return - the description as a string
     */
    public String getDescription() {
        return description;
    }

    /***
     * Getter for the operation type
     * As I said in operation i have a string for each
     * operation type to recognize them
     * @return - cardPayment
     */
    @Override
    public String getOperationType() {
        return "cardPayment";
    }
}
