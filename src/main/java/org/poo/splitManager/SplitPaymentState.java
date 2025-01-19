package org.poo.splitManager;

import org.poo.data.Account;
import org.poo.data.Operation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the state of a split payment, including:
 *  - the split type ("custom"/"equal")
 *  - the participating accounts
 *  - the map (Account -> double amount)
 *  - a reference to the pending Operation (which might be SplitEqualPaymentOperation or SplitCustomPaymentOperation)
 *  - the accepted/rejected status
 *  - the creation timestamp
 */
public class SplitPaymentState {
    private final String splitPaymentType;      // "custom" or "equal"
    private final Set<Account> allAccounts;     // the accounts involved
    private final Map<Account, Double> splitMap; // how much each account should pay
    private final Operation pendingOperation;   // now a generic Operation, not specifically SplitEqualPaymentOperation

    private final Set<Account> acceptedAccounts; // who has accepted
    private boolean rejected;                   // did someone reject?
    private boolean finalized;                  // is it finalized?

    private final int creationTimestamp;        // for logging if needed

    /**
     * Constructs the split payment state.
     *
     * @param splitPaymentType   "custom" or "equal"
     * @param allAccounts        the set of participating accounts
     * @param splitMap           map of each account to the amount it owes
     * @param pendingOperation   the generic Operation object (could be custom or equal)
     * @param creationTimestamp  the time when this split was created
     */
    public SplitPaymentState(String splitPaymentType,
                             Set<Account> allAccounts,
                             Map<Account, Double> splitMap,
                             Operation pendingOperation,
                             int creationTimestamp) {
        this.splitPaymentType = splitPaymentType;
        this.allAccounts = allAccounts;
        this.splitMap = splitMap;
        this.pendingOperation = pendingOperation;

        this.acceptedAccounts = new HashSet<>();
        this.rejected = false;
        this.finalized = false;
        this.creationTimestamp = creationTimestamp;
    }

    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    public Set<Account> getAllAccounts() {
        return allAccounts;
    }

    public Map<Account, Double> getSplitMap() {
        return splitMap;
    }

    public Operation getPendingOperation() {
        return pendingOperation;
    }

    public int getCreationTimestamp() {
        return creationTimestamp;
    }

    public boolean isRejected() {
        return rejected;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    /**
     * Marks the given account as accepting. If it's already rejected or finalized, throw an exception.
     */
    public void accept(Account account) {
        if (rejected) {
            throw new IllegalStateException("This split was already rejected by someone.");
        }
        if (finalized) {
            throw new IllegalStateException("This split is already finalized.");
        }
        if (!allAccounts.contains(account)) {
            throw new IllegalArgumentException("Account not part of this split.");
        }
        acceptedAccounts.add(account);
    }

    /**
     * Marks the split payment as rejected, preventing it from finalizing.
     */
    public void reject() {
        if (finalized) {
            throw new IllegalStateException("Cannot reject a finalized split payment.");
        }
        rejected = true;
    }

    /**
     * Checks if all participants have accepted (and no reject).
     */
    public boolean isFullyAccepted() {
        return !rejected && !finalized && acceptedAccounts.containsAll(allAccounts);
    }
}
