package org.poo.cardTypes;

import org.poo.data.Account;
import org.poo.data.Card;

public class OneTimeCard extends Card {
    public OneTimeCard(final String cardNumber) {
        super(cardNumber);
    }

    /***
     * This is where we perform the payment
     * basically removing the funds from the card's account
     * Here cause its an one time i also have to put up a new
     * card number for the card, I dont delete it and make a new
     * one I just modify the number
     * @param amount - the amount to be removed from the acc
     * @param account - the acc that has the given card
     */
    @Override
    public void payOnline(final double amount, final Account account) {
        if (account.getBalance() < amount) {
            // to pass this in FailOperation
            throw new IllegalArgumentException("Insufficient funds");
        }

        // here is where the amount is removed from the account
        account.removeFunds(amount);

        String newCardNumber = org.poo.utils.Utils.generateCardNumber();
        this.setCardNumber(newCardNumber);
    }

    /***
     * I have this to know what type of card im dealing with
     * @return -OneTime
     */
    @Override
    public String getCardType() {
        return "OneTime";
    }
}


