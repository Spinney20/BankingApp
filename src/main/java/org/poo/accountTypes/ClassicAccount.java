package org.poo.accountTypes;

import org.poo.data.Account;

public class ClassicAccount extends Account {

    public ClassicAccount(final String iban, final String currency) {
        super(iban, currency);
    }

    /***
     * I use this to know which type of account im dealing with
     * of copurse, to avoid instance of
     * @return - classic for classic ofc
     */
    @Override
    public String getAccountType() {
        return "classic";
    }

    /***
     * Classic acounts don't have interest sooo
     * @return - i just return 0.0
     */
    @Override
    public double calculateInterest() {
        return 0.0; // Classic accounts do not have interest
    }

    /***
     * Again, classic acc don;t have interest so I just thought ab
     * adding an exception that I will never use :)
     * @param interestRate - just for the plot
     */
    @Override
    public void setInterestRate(final double interestRate) {
        throw new UnsupportedOperationException("Classic accounts do not support interest rates.");
    }

    @Override
    public boolean isBusinessAccount() {
        return false;
    }

}
