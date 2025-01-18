package org.poo.factories;

import org.poo.data.Account;
import org.poo.accountTypes.ClassicAccount;
import org.poo.accountTypes.SavingsAccount;

public final class AccountFactory {
    // Hide the constructor for the checkstyle
    private AccountFactory() {
        throw new UnsupportedOperationException("utility class = no instatiation");
    }

    /**
     * Creating an account of a given type
     * the type dictates what account I should create
     * @param type - type of the account to create
     * @param iban - the iban of the acc I should create
     * @param currency - the currency of the account
     * @return - the account created
     */
    public static Account createAccount(final String type, final String iban,
                                        final String currency, final double interestRate) {
        switch (type.toLowerCase()) {
            case "classic":
                return new ClassicAccount(iban, currency);
            case "savings":
                return new SavingsAccount(iban, currency, interestRate);
            case "business":
                return new SavingsAccount(iban, currency, interestRate);
            default:
                throw new IllegalArgumentException("Unknown account type: " + type);
        }
    }
}
