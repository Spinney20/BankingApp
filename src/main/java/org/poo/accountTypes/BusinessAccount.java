package org.poo.accountTypes;

import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Stats;
import org.poo.data.User;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BusinessAccount extends Account {
    private final String ownerEmail;
    private final Map<String, String> associates; // Email -> Role (manager, employee)
    private static final double DEFAULT_LIMIT = 500.0;
    private Map<String, Double> commerciants;
    private final Map<String, Stats> statsMap;
    // Key: commerciantName, Value: Map<userEmail -> totalSpentByThatUserOnThisCommerciant>
    private Map<String, Map<String, Double>> userSpentOnCommerciant = new LinkedHashMap<>();
    // For each merchantName, we keep a map of (userEmail -> integer # of transactions)
    private Map<String, Map<String, Integer>> userTxCountOnCommerciant = new LinkedHashMap<>();


    private double globalSpendingLimit; // Global spending limit in account's currency
    private double globalDepositLimit;  // Global deposit limit in account's currency

    public BusinessAccount(final String iban, final String currency, final String ownerEmail,
                           final ExchangeRateManager exchangeRateManager) {
        super(iban, currency);
        this.ownerEmail = ownerEmail;
        this.associates = new LinkedHashMap<>();
        this.commerciants = new HashMap<>();
        this.statsMap = new HashMap<>();
        this.userSpentOnCommerciant = new LinkedHashMap<>();
        this.userTxCountOnCommerciant = new LinkedHashMap<>();

        this.globalSpendingLimit =
                convertDefaultLimit(exchangeRateManager, DEFAULT_LIMIT, currency);
        this.globalDepositLimit =
                convertDefaultLimit(exchangeRateManager, DEFAULT_LIMIT, currency);
    }

    private double convertDefaultLimit(final ExchangeRateManager exchangeRateManager,
                                       final double defaultLimit, final String currency) {
        if ("RON".equalsIgnoreCase(currency)) {
            return defaultLimit;
        }

        double exchangeRate = exchangeRateManager.getExchangeRate("RON", currency);
        if (exchangeRate == -1) {
            throw new IllegalStateException("Exchange rate not available for " + currency);
        }

        return defaultLimit * exchangeRate;
    }

    /***
     * Adds an associate to the account.
     * Associates can be either managers or employees.
     * I need this because in business raport I need to know if the user is an employee or a manager
     * And alsooo i need them all so i can shoecase them
     * @param email
     * @param role
     * @param associate
     */
    public void addAssociate(final String email, final String role, final User associate) {
        if (associates.containsKey(email)) {
            throw new IllegalArgumentException("The user is already an associate of the account.");
        }
        associates.put(email, role);
        System.out.println("Associate pus in map cu rol " + role);

        if (associate != null) {
            associate.addAccount(this);
        }
    }

    /***
     * This just changes the spending limit of the account, basically a setter
     * @param newLimit - new limit to be set
     */
    public void changeGlobalSpendingLimit(final double newLimit) {
        this.globalSpendingLimit = newLimit;
    }

    /***
     * setter for the deposit limit
     * @param newLimit
     */
    public void changeGlobalDepositLimit(final double newLimit) {
        this.globalDepositLimit = newLimit;
    }

    /***
     * getter for the spending limit
     * @return
     */
    public double getGlobalSpendingLimit() {
        return globalSpendingLimit;
    }

    /***
     * getter for the deposit limit
     * @return
     */
    public double getGlobalDepositLimit() {
        return globalDepositLimit;
    }

    /***
     * getter for the associates map
     * @return - the associates map
     */
    public Map<String, String> getAssociates() {
        return associates;
    }

    /***
     * method to check if the user is the owner of the account
     * used widely in the code for multiple checks
     * @param email - the email of the user
     * @return - true if the user is the owner, false otherwise
     */
    public boolean isOwner(final String email) {
        return ownerEmail.equals(email);
    }

    /***
     * same as the isOwner method, but for associates
     * @param email - the email of the user
     * @return - true if the user is an associate, false otherwise
     */
    public boolean isAssociate(final String email) {
        return associates.containsKey(email);
    }

    /***
     * overriden method from the Account class
     * @return - the type of the account
     */
    @Override
    public String getAccountType() {
        return "business";
    }

    /***
     * overriden method from the Account class
     * @return - 0 cause business accounts don't have interest rates
     */
    @Override
    public double calculateInterest() {
        return 0.0; // Conturile business nu au
    }

    /***
     * overriden method from the Account class
     * @param interestRate - exception thrown because business accounts don't have interest rates
     */
    @Override
    public void setInterestRate(final double interestRate) {
        throw new UnsupportedOperationException("Business accounts do"
                + " not support interest rates.");
    }

    /***
     * very important method for the business account
     * In business report i need to know how much money was spent on each commerciant
     * in the commerciant report soo this is crucial
     * @param commerciantName - the name of the commerciant
     * @param amount - the amount of money spent
     * @param userEmail - the email of the user that spent the money
     */
    @Override
    public void addCommerciantTransaction(final String commerciantName, final double amount,
                                          final String userEmail) {

        // increment total for the entire business
        commerciants.put(commerciantName,
                commerciants.getOrDefault(commerciantName, 0.0) + amount
        );

        // also record user-specific spending:
        userSpentOnCommerciant.putIfAbsent(commerciantName, new LinkedHashMap<>());
        Map<String, Double> userMap = userSpentOnCommerciant.get(commerciantName);
        userMap.put(userEmail, userMap.getOrDefault(userEmail, 0.0) + amount);

        userTxCountOnCommerciant.putIfAbsent(commerciantName, new LinkedHashMap<>());
        Map<String, Integer> txCountMap = userTxCountOnCommerciant.get(commerciantName);
        txCountMap.put(userEmail, txCountMap.getOrDefault(userEmail, 0) + 1);
    }

    /***
     * getter for the commerciants map
     * used in the business report
     * @return - the commerciants map
     */
    public Map<String, Double> getCommerciants() {
        return commerciants;
    }

    /***
     * overriden method from the Account class
     * this is used in the business report
     * because i have to know how much money was spent by each user
     * @param userEmail - the email of the user
     * @param amount - the amount of money spent
     */
    @Override
    public void addSpent(final String userEmail, final double amount) {
        if (amount <= 0 || userEmail == null) {
            return;
        }
        statsMap.putIfAbsent(userEmail, new Stats());
        Stats stats = statsMap.get(userEmail);
        stats.addSpent(amount);
    }

    /***
     * pretty much same as add spent but for deposits
     * @param userEmail - the email of the user
     * @param amount - the amount of money deposited
     */
    @Override
    public void addDeposit(final String userEmail, final double amount) {
        if (amount <= 0 || userEmail == null) {
            return;
        }
        statsMap.putIfAbsent(userEmail, new Stats());
        Stats stats = statsMap.get(userEmail);
        stats.addDeposited(amount);
    }

    /***
     * getter for the stats map
     * @return - the stats map
     */
    public Map<String, Stats> getStatsMap() {
        return statsMap;
    }

    /***
     * overriden method from the Account class
     * used to avoid instanceof
     * @return - true because this is a business account
     */
    @Override
    public boolean isBusinessAccount() {
        return true;
    }

    /***
     * overriden method from the Account class
     * @param user - the user
     * @return - true if the user is an employee, false otherwise
     */
    public boolean isEmployee(final User user) {
        if (user == null) {
            return false;
        }
        String role = associates.get(user.getEmail());
        if (role == null) {
            return false;
        }
        role = role.trim(); // remove extra spaces just in case
        return role.equalsIgnoreCase("employee");
    }

    /***
     * Used in the business report
     * getter for the user spent on commerciant map
     * @return - the map of the user spent on commerciant
     */
    public Map<String, Map<String, Double>> getUserSpentOnCommerciant() {
        return userSpentOnCommerciant;
    }

    /***
     * Used in the business report
     * getter for the user transaction count on commerciant map
     * made this because if i have 2 transactions at same commerciant
     * i have to showcase twice in business report
     * @return
     */
    public Map<String, Map<String, Integer>> getUserTxCountOnCommerciant() {
        return userTxCountOnCommerciant;
    }

}
