package org.poo.factories;

import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.accountTypes.ClassicAccount;
import org.poo.accountTypes.SavingsAccount;
import org.poo.accountTypes.BusinessAccount;

public final class AccountFactory {

    /**
     * Creates an account of a given type.
     * The type dictates what kind of account to create.
     *
     * @param type      The type of account to create (e.g., "classic", "savings", "business")
     * @param iban      The IBAN of the account to create
     * @param currency  The currency of the account
     * @param ownerEmail The email of the owner (used only for BusinessAccount)
     * @param interestRate The interest rate (used only for SavingsAccount)
     * @return The created account
     */
    public static Account createAccount(
            final String type,
            final String iban,
            final String currency,
            final String ownerEmail,
            final double interestRate,
            final ExchangeRateManager exchangeRateManager) { // Added ExchangeRateManager

        switch (type.toLowerCase()) {
            case "classic":
                return new ClassicAccount(iban, currency);
            case "savings":
                return new SavingsAccount(iban, currency, interestRate);
            case "business":
                return new BusinessAccount(iban, currency, ownerEmail, exchangeRateManager);
            default:
                throw new IllegalArgumentException("Unknown account type: " + type);
        }
    }
}
