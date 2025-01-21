package org.poo.data;

import org.poo.factories.CardFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Account {
    protected String iban;
    protected String currency;
    protected double balance;
    protected List<Card> cards;
    protected List<Operation> operations;
    private double minBalance;
    private String alias;

    // Cashback-related fields
    // Tracks number of transactions per merchant
    private Map<String, Integer> merchantTransactionCount;
    private Map<String, Double> spendingTotals; // Tracks total spending per category
    private Map<String, Boolean> cashbackApplied; // Tracks if cashback was applied per category
    private Map<String, Double> merchantSpending;
    private Map<String, Boolean> nrOfTransactionsCashbackUsed;

    // Pending operations for split payments
    private List<Operation> pendingOperations;
    private double totalSpentOnTresholdCashback;

    // Constructor
    public Account(final String iban, final String currency) {
        this.iban = iban;
        this.currency = currency;
        this.balance = 0.0;
        this.cards = new ArrayList<>();
        this.operations = new ArrayList<>();
        this.minBalance = 0.0;

        // Initialize cashback-related maps
        this.merchantTransactionCount = new HashMap<>();
        this.spendingTotals = new HashMap<>();
        this.cashbackApplied = new HashMap<>();
        this.merchantSpending = new HashMap<>();
        this.nrOfTransactionsCashbackUsed = new HashMap<>();

        // Initialize pending operations
        this.pendingOperations = new ArrayList<>();
    }

    /***
     * getter for IBAN
     * @return - returning the iban of the acc
     */
    public String getIban() {
        return iban;
    }

    /***
     * getter for the currency of the account
     * @return - returning the currency
     */
    public String getCurrency() {
        return currency;
    }

    /***
     * getter for the balance
     * @return
     */
    public double getBalance() {
        return balance;
    }

    /***
     * getter for the list of cards of the account
     * @return
     */
    public List<Card> getCards() {
        return cards;
    }

    /***
     * getter for the list of operations of the account
     * @return
     */
    public List<Operation> getOperations() {
        return operations;
    }

    /***
     * The method for adding funds to the account
     * it is used when calling the addFunds command
     * and also when receiving money or interest
     * @param amount - the amount to be added
     */
    public void addFunds(final double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    /***
     * The method for removing funds from the account
     * It is used when paying online, sending money spent payment etc
     * @param amount - the amount to be removed
     */
    public void removeFunds(final double amount) {
        this.balance -= amount;
    }

    /***
     * Adding an operation in the list of operations
     * to later be called in print transactions or reports
     * @param operation - the operation to be added
     */
    public void addOperation(final Operation operation) {
        operations.add(operation);
    }

    /***
     * Adding a card to the account
     * Calling the factory to create the card
     * and the adding the created card to the account
     * @param type - the type of the card to be added
     * @param cardNumber - the number of the card to be added
     */
    public void addCard(final String type, final String cardNumber) {
        Card card = CardFactory.createCard(type, cardNumber);
        cards.add(card);
    }

    /***
     * Deleting a card from the account
     * Finding the card that should be removed and removing it
     * Also I have an exception for not finding it just for the plot
     * @param cardNumber - the number of the card to be deleted
     */
    public void deleteCard(final String cardNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                cards.remove(card);
                return;
            }
        }
        throw new IllegalArgumentException("Card not found.");
    }

    /***
     * Getter for card with a given nr
     * @param cardNumber - the number of the card i want
     * @return - returning the card with the given number
     */
    public Card getCard(final String cardNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }

    /***
     * Setter for the min balance
     * @param minBalance
     */
    public void setMinBalance(final double minBalance) {
        this.minBalance = minBalance;
    }

    /***
     * Getter for the min balance
     * @return
     */
    public double getMinBalance() {
        return minBalance;
    }

    /***
     * Setter for the alias of the acc
     * @param alias
     */
    public void setAlias(final String alias) {
        this.alias = alias;
    }

    /***
     * Mark cashback as applied for a specific category.
     * This is used to prevent multiple cashback applications for the same category.
     * @param category - the category name
     */
    public void applyCashback(final String category) {
        cashbackApplied.put(category, true);
    }

    /***
     * Add a pending operation to the account.
     * @param operation - the operation to be added to pending.
     */
    public void addPendingOperation(final Operation operation) {
        pendingOperations.add(operation);
    }

    /***
     * Remove a pending operation from the account.
     * @param operation - the operation to be removed.
     */
    public void removePendingOperation(final Operation operation) {
        pendingOperations.remove(operation);
    }

    /***
     * Getter for nrOfTransactionsCashbackUsed.
     * @param category - the category name.
     * @return - true if the cashback was used, false otherwise.
     */
    public boolean hasUsedNrOfTransactionsCashback(final String category) {
        return nrOfTransactionsCashbackUsed.getOrDefault(category, false);
    }

    /***
     * Mark nrOfTransactionsCashback as used for a specific category.
     * @param category - the category name.
     */
    public void markNrOfTransactionsCashbackAsUsed(final String category) {
        nrOfTransactionsCashbackUsed.put(category, true);
    }

    /***
     * Abstract method to be overridden in each account type
     * so i know the type of the account and not use instance of
     * @return - the type of the acc - savings or classic
     */
    public abstract String getAccountType();

    /***
     * Savings accounts have interest but classic dont
     * @return
     */
    public abstract double calculateInterest();

    /***
     * Savings accounts have interest but classic dont
     * @param interestRate
     */
    public abstract void setInterestRate(double interestRate);

    public void addMerchantSpending(final String merchantName, final double amount) {
        double currentSpending = merchantSpending.getOrDefault(merchantName, 0.0);
        merchantSpending.put(merchantName, currentSpending + amount);
    }


    public void addCommerciantTransaction(final String merchantName, final double amount,
                                          final String userEmail) {
        // nu face nimic pentru conturile care nu sunt de tip business
    }

    public void addSpent(final String userEmail, final double amount) {
        // Implicit, conturile standard nu fac nimic
    }

    public void addDeposit(final String userEmail, final double amount) {
        // Implicit, conturile standard nu fac nimic
    }

    public double incrementTotalSpentOnTresholdCashback(final double amount) {
        totalSpentOnTresholdCashback += amount;
        return totalSpentOnTresholdCashback;
    }

    public double getTotalSpentOnTresholdCashback() {
        return totalSpentOnTresholdCashback;
    }

    public abstract boolean isBusinessAccount(); // metoda ca sa nu folosesc instanceof

}
