package org.poo.accountTypes;

import org.poo.data.Account;

public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final String iban, final String currency, final double interestRate) {
        super(iban, currency);
        this.interestRate = interestRate; // Setăm rata dobânzii la inițializare
    }

    /***
     * Setter for the interest rate of a savings acc
     * Modifying the default interest or an already modified interest
     * with the given interest
     * @param interestRate - the given interest
     */
    public void setInterestRate(final double interestRate) {
        this.interestRate = interestRate;
    }

    /***
     * Getter for the interest rate of a savings account
     * @return - the interest rate
     */
    public double getInterestRate() {
        return interestRate;
    }

    /***
     * This is how I calculate interest
     * @return - returning the value computed
     */
    public double calculateInterest() {
        return balance * (interestRate / 100);
    }

    /***
     * Just like the classic account, I have a method to know the type
     * of the savings account too. This is useful when performing operations
     * @return - "savings"
     */
    @Override
    public String getAccountType() {
        return "savings";
    }
}
