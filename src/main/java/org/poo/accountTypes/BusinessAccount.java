package org.poo.accountTypes;

import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Stats;
import org.poo.data.User;

import java.util.HashMap;
import java.util.Map;

public class BusinessAccount extends Account {
    private final String ownerEmail;
    private final Map<String, String> associates; // Email -> Role (manager, employee)
    private static final double DEFAULT_LIMIT = 500.0;
    private Map<String, Double> commerciants;
    private final Map<String, Stats> statsMap;

    private double globalSpendingLimit; // Global spending limit in account's currency
    private double globalDepositLimit;  // Global deposit limit in account's currency

    public BusinessAccount(String iban, String currency, String ownerEmail,  ExchangeRateManager exchangeRateManager) {
        super(iban, currency);
        this.ownerEmail = ownerEmail;
        this.associates = new HashMap<>();
        this.commerciants = new HashMap<>();
        this.statsMap = new HashMap<>();

        this.globalSpendingLimit = convertDefaultLimit(exchangeRateManager, DEFAULT_LIMIT, currency);
        this.globalDepositLimit = convertDefaultLimit(exchangeRateManager, DEFAULT_LIMIT, currency);
    }

    private double convertDefaultLimit(ExchangeRateManager exchangeRateManager, double defaultLimit, String currency) {
        if ("RON".equalsIgnoreCase(currency)) {
            return defaultLimit; // Daca moneda este deja RON, nu facem conversie
        }

        double exchangeRate = exchangeRateManager.getExchangeRate("RON", currency);
        if (exchangeRate == -1) {
            throw new IllegalStateException("Exchange rate not available for " + currency);
        }

        return defaultLimit * exchangeRate;
    }

    public void addAssociate(String email, String role, User associate) {
        if (associates.containsKey(email)) {
            throw new IllegalArgumentException("The user is already an associate of the account.");
        }
        associates.put(email, role);

        // Adaugă acest cont în lista de conturi a utilizatorului asociat
        if (associate != null) {
            associate.addAccount(this); // `this` este contul BusinessAccount curent
        }
    }


    public void changeGlobalSpendingLimit(double newLimit) {
        this.globalSpendingLimit = newLimit;
    }

    public void changeGlobalDepositLimit(double newLimit) {
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

    public boolean isOwner(String email) {
        return ownerEmail.equals(email);
    }

    public boolean isAssociate(String email) {
        return associates.containsKey(email);
    }

    public String getRole(String email) {
        return associates.get(email);
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
    public void setInterestRate(double interestRate) {
        throw new UnsupportedOperationException("Business accounts do not support interest rates.");
    }

    @Override
    public void addCommerciantTransaction(String commerciantName, double amount) {

        commerciants.put(commerciantName, commerciants.getOrDefault(commerciantName, 0.0) + amount);
    }

    public Map<String, Double> getCommerciants() {
        return commerciants;
    }

    @Override
    public void addSpent(String userEmail, double amount) {
        if (amount <= 0 || userEmail == null) return;

        statsMap.putIfAbsent(userEmail, new Stats());
        Stats stats = statsMap.get(userEmail);
        stats.addSpent(amount);
    }

    @Override
    public void addDeposit(String userEmail, double amount) {
        if (amount <= 0 || userEmail == null) return;

        statsMap.putIfAbsent(userEmail, new Stats());
        Stats stats = statsMap.get(userEmail);
        stats.addDeposited(amount);
    }

    public Map<String, Stats> getStatsMap() {
        return statsMap;
    }

    // Convert default limits into the account's currency
    public double getConvertedDepositLimit(ExchangeRateManager exchangeRateManager) {
        return convertDefaultLimit(exchangeRateManager, DEFAULT_LIMIT);
    }

    public double getConvertedSpendingLimit(ExchangeRateManager exchangeRateManager) {
        return convertDefaultLimit(exchangeRateManager, DEFAULT_LIMIT);
    }

    private double convertDefaultLimit(ExchangeRateManager exchangeRateManager, double defaultLimit) {

        double exchangeRate = exchangeRateManager.getExchangeRate("RON", getCurrency());
        if (exchangeRate == -1) {
            throw new IllegalStateException("Exchange rate not available for " + getCurrency());
        }

        return defaultLimit * exchangeRate; // Convert the default limit to the account's currency
    }
}
