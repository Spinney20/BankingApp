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

    public Commerciant(final String name, final String category, final String cashbackType,
                       final String account) {
        this.name = name;
        this.category = category;
        this.cashbackType = cashbackType;
        this.account = account;
        this.transactionCounts = new HashMap<>();
        merchants.put(name, this);
    }

    /***
     * getting commerciant by string name
     * @param name
     * @return
     */
    public static Commerciant getMerchantByName(final String name) {
        return merchants.get(name);
    }

    /***
     * getting category of commerciant
     * @return
     */
    public String getCategory() {
        return category;
    }

    /***
     * getter for cashback type
     * @return
     */
    public String getCashbackType() {
        return cashbackType;
    }

    /***
     * getter for account
     * @return
     */
    public String getAccount() {
        return account;
    }

    /***
     * getter for name
     * @return
     */
    public String getName() {
        return name;
    }

    /***
     * getter for transaction counts
     * @return
     */
    public int incrementAndGetTransactionCount(final Account account) {
        int currentCount = transactionCounts.getOrDefault(account, 0);
        transactionCounts.put(account, currentCount + 1);
        return currentCount + 1;
    }
}
