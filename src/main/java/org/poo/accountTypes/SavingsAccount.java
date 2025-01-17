package org.poo.accountTypes;

import org.poo.data.Account;

public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final String iban, final String currency) {
        super(iban, currency);
        this.interestRate = 0.0; //default intterst
    }

    /***
     * Setter for the interest rate of a savings acc
     * Modifying the default interest or an alr modifief interest
     * with the given interest
     * @param interestRate - the given interest
     */
    public void setInterestRate(final double interestRate) {
        this.interestRate = interestRate;
    }

    /***
     * This is how I calcuulate interest
     * @return - returning the value computed
     */
    public double calculateInterest() {
        return balance * (interestRate / 100);
    }

    /***
     * Just like the classic acc i have a method to know the type
     * of the savings acc too, in the spenings report i have to put up
     * an error cause i cant make a spendings report for a savings acc
     * so I have this so i dont use instance of
     * @return - savings
     */
    @Override
    public String getAccountType() {
        return "savings";
    }
}
