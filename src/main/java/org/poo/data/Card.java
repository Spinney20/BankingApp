package org.poo.data;

public abstract class Card {
    private String cardNumber;
    private String status;
    private boolean isFrozen;

    // constructor of the card
    public Card(final String cardNumber) {
        this.cardNumber = cardNumber;
        this.status = "active";
        this.isFrozen = false;
    }

    /***
     * Check if a card is frozen
     * @return - true if its frozen false otherwise
     */
    public boolean isFrozen() {
        return isFrozen;
    }

    /***
     * Basically a setter for the freeze
     * And also sets the status to frozen
     */
    public void freeze() {
        this.isFrozen = true;
        this.status = "frozen";
    }

    /***
     * getter for the status
     * @return - status as a string
     */
    public String getStatus() {
        return status;
    }

    /***
     * getter for the card number
     * @return - the card nr as a string
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /***
     * Setter for the card number
     * I use this to set a new card number for the one time card
     * when paying and I need a new card number
     * @param cardNumber - card nr to be set
     */
    public void setCardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
    }

    /***
     * Paying online method depends on the type of the card
     * If its one time I also have to change the card nr
     * @param amount - amount to be paid
     * @param account - owner of the card
     */
    public abstract void payOnline(double amount, Account account);

    /***
     * getter for the card type
     * @return - regular or oneTime
     */
    public abstract String getCardType();
}

