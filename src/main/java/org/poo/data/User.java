package org.poo.data;

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

    // Constructor
    public User(final String firstName, final String lastName, final String email, final LocalDate birthDate, final String occupation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.occupation = occupation;
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
}
