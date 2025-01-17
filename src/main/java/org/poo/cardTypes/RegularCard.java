package org.poo.cardTypes;

import org.poo.data.Account;
import org.poo.data.Card;

public class RegularCard extends Card {
    public RegularCard(final String cardNumber) {
        super(cardNumber);
    }

    /***
     * Here is where I perform the paying
     * removing the funds from the card's acc
     * also I have an exception if the acc has not enough money
     * which I pass to to fail Operation when Im making it, pretty nice
     * @param amount - the amount to be removed from the acc
     * @param account - the acc that holds the card
     */
    @Override
    public void payOnline(final double amount, final Account account) {

        if (account.getBalance() < amount) {
            // to pass this in FailOperation
            throw new IllegalArgumentException("Insufficient funds");
        }

        // here is where the amount is removed from the account
        account.removeFunds(amount);
    }

    /***
     * I have this to know what type of card Im dealing with
     * @return - regular
     */
    @Override
    public String getCardType() {
        return "Regular";
    }
}


