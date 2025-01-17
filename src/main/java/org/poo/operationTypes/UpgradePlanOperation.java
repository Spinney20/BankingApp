package org.poo.operationTypes;

import org.poo.data.Operation;

public class UpgradePlanOperation extends Operation {
    private String accountIBAN;
    private String newPlanType;
    private double fee;
    private String currency;

    /**
     * Constructor for UpgradePlanOperation
     *
     * @param timestamp    The time of the operation
     * @param accountIBAN  The IBAN of the account being upgraded
     * @param newPlanType  The new plan type
     * @param fee          The fee for the upgrade
     * @param currency     The currency of the fee
     */
    public UpgradePlanOperation(final int timestamp, final String accountIBAN,
                                final String newPlanType, final String currency) {
        super(timestamp);
        this.accountIBAN = accountIBAN;
        this.newPlanType = newPlanType;
        this.fee = fee;
        this.currency = currency;
    }

    /**
     * Getter for the account IBAN
     *
     * @return The account IBAN
     */
    public String getAccountIBAN() {
        return accountIBAN;
    }

    /**
     * Getter for the new plan type
     *
     * @return The new plan type
     */
    public String getNewPlanType() {
        return newPlanType;
    }

    /**
     * Getter for the currency
     *
     * @return The currency of the fee
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Getter for the operation type
     *
     * @return The type of the operation as a string
     */
    @Override
    public String getOperationType() {
        return "upgradePlan";
    }
}

