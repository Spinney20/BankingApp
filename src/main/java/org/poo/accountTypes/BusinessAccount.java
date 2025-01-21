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

    public void changeGlobalSpendingLimit(final double newLimit) {
        this.globalSpendingLimit = newLimit;
    }

    public void changeGlobalDepositLimit(final double newLimit) {
        this.globalDepositLimit = newLimit;
    }

    public double getGlobalSpendingLimit() {
        return globalSpendingLimit;
    }

    public double getGlobalDepositLimit() {
        return globalDepositLimit;
    }

    public Map<String, String> getAssociates() {
        return associates;
    }

    public boolean isOwner(final String email) {
        return ownerEmail.equals(email);
    }

    public boolean isAssociate(final String email) {
        return associates.containsKey(email);
    }

    @Override
    public String getAccountType() {
        return "business";
    }

    @Override
    public double calculateInterest() {
        return 0.0; // Conturile business nu au
    }

    @Override
    public void setInterestRate(final double interestRate) {
        throw new UnsupportedOperationException("Business accounts do"
                + " not support interest rates.");
    }

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

    public Map<String, Double> getCommerciants() {
        return commerciants;
    }

    @Override
    public void addSpent(final String userEmail, final double amount) {
        if (amount <= 0 || userEmail == null) {
            return;
        }
        statsMap.putIfAbsent(userEmail, new Stats());
        Stats stats = statsMap.get(userEmail);
        stats.addSpent(amount);
    }

    @Override
    public void addDeposit(final String userEmail, final double amount) {
        if (amount <= 0 || userEmail == null) {
            return;
        }
        statsMap.putIfAbsent(userEmail, new Stats());
        Stats stats = statsMap.get(userEmail);
        stats.addDeposited(amount);
    }

    public Map<String, Stats> getStatsMap() {
        return statsMap;
    }

    @Override
    public boolean isBusinessAccount() {
        return true;
    }

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

    public Map<String, Map<String, Double>> getUserSpentOnCommerciant() {
        return userSpentOnCommerciant;
    }

    public Map<String, Map<String, Integer>> getUserTxCountOnCommerciant() {
        return userTxCountOnCommerciant;
    }

}
