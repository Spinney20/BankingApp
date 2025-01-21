package org.poo.data;

public class Stats {
    private double spent;
    private double deposited;

    public Stats() {
        this.spent = 0.0;
        this.deposited = 0.0;
    }

    public double getSpent() {
        return spent;
    }

    public void addSpent(final double amount) {
        this.spent += amount;
    }

    public double getDeposited() {
        return deposited;
    }

    public void addDeposited(final double amount) {
        this.deposited += amount;
    }
}

