package org.poo.data;

import org.poo.servicePlan.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private String occupation;
    private List<Account> accounts = new ArrayList<>();
    private TransactionService transactionService; // Decorator for cashback and commission logic

    // Constructor
    public User(final String firstName, final String lastName, final String email, final LocalDate birthDate, final String occupation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.occupation = occupation;

        // Default plan based on occupation
        this.transactionService = occupation.equalsIgnoreCase("student")
                ? new StudentPlanDecorator(new BaseTransactionService())
                : new StandardPlanDecorator(new BaseTransactionService());
    }

    /**
     * Getter for the first name
     * @return - first name as string
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Getter for the last name
     * @return - last name as string
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Getter for the email of the user
     * @return - the email as a string
     */
    public String getEmail() {
        return email;
    }

    /**
     * Getter for the birth date of the user
     * @return - the birth date as a LocalDate
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * Getter for the occupation of the user
     * @return - the occupation as a string
     */
    public String getOccupation() {
        return occupation;
    }

    /**
     * Calculates the user's age based on their birth date
     * @return - the user's age as an integer
     */
    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Getter for the list of the accounts of the user
     * @return - the accounts
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Method to add an account to the user
     * @param account - the account to be added
     */
    public void addAccount(final Account account) {
        this.accounts.add(account);
    }

    /**
     * Method for deleting an account from the user's list
     * Also throws an exception if the account still has money,
     * because we can't delete an account that still has money.
     * @param iban - the IBAN of the card to be deleted
     * @return true if it's deleted; throws exception if something is not ok
     */
    public boolean deleteAccount(final String iban) {
        for (Account account : accounts) {
            if (account.getIban().equals(iban)) {
                if (account.getBalance() != 0) {
                    throw new IllegalArgumentException("Account couldn't "
                            + "be deleted - see org.poo.transactions for details");
                }
                accounts.remove(account);
                return true; // account deleted
            }
        }
        throw new IllegalArgumentException("Account not found.");
    }

    /**
     * Applies cashback based on the current user's plan.
     * @param totalSpending - total spending for cashback calculation
     * @param transactionAmount - the amount of the transaction
     * @return - the cashback amount
     */
    public double applyCashback(double totalSpending, double transactionAmount) {
        return transactionService.applyCashback(totalSpending, transactionAmount);
    }

    /**
     * Applies commission based on the current user's plan.
     * @param transactionAmount - the amount of the transaction
     * @return - the commission amount
     */
    public double applyCommission(double transactionAmount) {
        return transactionService.applyCommission(transactionAmount);
    }

    /**
     * Upgrades the user's plan to a new plan.
     * @param newPlanType - the new plan type
     */
    public String upgradePlan(String newPlanType) {
        String normalizedCurrentPlan = getCurrentPlanName().toLowerCase();
        newPlanType = newPlanType.toLowerCase();

        // Check if the user is already on the new plan
        if (normalizedCurrentPlan.equals(newPlanType)) {
            return "The user already has the " + newPlanType + " plan.";
        }

        // Prevent downgrades
        if ((normalizedCurrentPlan.equals("gold") && !newPlanType.equals("gold"))
                || (normalizedCurrentPlan.equals("silver") && newPlanType.equals("standard"))) {
            return "You cannot downgrade your plan.";
        }

        // Apply upgrade
        switch (newPlanType) {
            case "silver" -> this.transactionService = new SilverPlanDecorator(transactionService);
            case "gold" -> this.transactionService = new GoldPlanDecorator(transactionService);
            default -> { return "Invalid plan type: " + newPlanType; }
        }
        return "Upgrade successful to " + newPlanType + " plan.";
    }


    /**
     * Returns the name of the current plan.
     * @return - the plan name
     */
    public String getCurrentPlanName() {
        return transactionService.getClass().getSimpleName().replace("PlanDecorator", "");
    }
}
