package org.poo.factories;

import org.poo.data.Card;
import org.poo.cardTypes.OneTimeCard;
import org.poo.cardTypes.RegularCard;

public final class CardFactory {
    //Hide the constructor
    private CardFactory() {
        throw new UnsupportedOperationException("utility class = no instatiation");
    }
    /***
     * Factory for cards
     * Creating a card of a given type
     * the tyoe dictates what card I should create
     * @param type regular or onetime
     * @param cardNumber - the card number of the card
     * @return - the card created
     */
    public static Card createCard(final String type, final String cardNumber) {
        switch (type.toLowerCase()) {
            case "regular":
                return new RegularCard(cardNumber);
            case "onetime":
                return new OneTimeCard(cardNumber);
            default:
                return null;
        }
    }
}
