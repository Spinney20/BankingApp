package org.poo.data;

import java.util.HashMap;
import java.util.Map;

public class Commerciant {
    private static Map<String, Commerciant> merchants = new HashMap<>();

    private String name;
    private String category; // Food, Clothes, Tech
    private String cashbackType; // nrOfTransactions or spendingThreshold
    private String account; // IBAN-ul comerciantului
    private Map<Account, Integer> transactionCounts; // Transactions per account

    // Constructor
    public Commerciant(String name, String category, String cashbackType, String account) {
        this.name = name;
        this.category = category;
        this.cashbackType = cashbackType;
        this.account = account;
        this.transactionCounts = new HashMap<>();
        merchants.put(name, this);
    }

    // Getter pentru un comerciant după nume
    public static Commerciant getMerchantByName(String name) {
        return merchants.get(name);
    }

    // Getter pentru categoria comerciantului (ex: Food, Tech etc.)
    public String getCategory() {
        return category;
    }

    // Getter pentru strategia de cashback a comerciantului
    public String getCashbackType() {
        return cashbackType;
    }

    // Getter pentru contul comerciantului
    public String getAccount() {
        return account;
    }

    // Getter pentru numele comerciantului
    public String getName() {
        return name;
    }

    public int incrementAndGetTransactionCount(Account account) {
        int currentCount = transactionCounts.getOrDefault(account, 0);
        transactionCounts.put(account, currentCount + 1);
        return currentCount + 1;
    }
}
