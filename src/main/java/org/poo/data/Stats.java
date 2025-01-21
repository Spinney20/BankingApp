package org.poo.data;

public class Stats {
    private double spent;
    private double deposited;

    public Stats() {
        this.spent = 0.0;
        this.deposited = 0.0;
    }

    /***
     * Getter for the spent amount
     * @return
     */
    public double getSpent() {
        return spent;
    }

    /***
     * adding the spent amount
     * @param amount
     */
    public void addSpent(final double amount) {
        this.spent += amount;
    }

    /***
     * getter of the sum of the deposited amount
     * @return
     */
    public double getDeposited() {
        return deposited;
    }

    /***
     * adding the deposited amount
     * @param amount
     */
    public void addDeposited(final double amount) {
        this.deposited += amount;
    }
}

