package org.poo.splitManager;

import org.poo.data.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Global manager for split payments:
 *  - Maintains a list of active "pending" split payment requests
 *  - At acceptance/rejection, it searches the first matching request
 *  - Finalizes the payment when all participants accept
 *  - Cancels it if any user rejects
 */
public class SplitPaymentManager {
    private static SplitPaymentManager instance;

    // A list of all pending splits (not yet finalized or rejected)
    private final List<SplitPaymentState> pendingSplits;

    private SplitPaymentManager() {
        this.pendingSplits = new ArrayList<>();
    }

    public static synchronized SplitPaymentManager getInstance() {
        if (instance == null) {
            instance = new SplitPaymentManager();
        }
        return instance;
    }

    /**
     * Adds a new split payment to the list of pending splits.
     */
    public void addSplit(SplitPaymentState state) {
        pendingSplits.add(state);
    }

    /**
     * Finds the first pending (not rejected, not finalized) split payment
     * of the given type where the specified account is participating.
     *
     * @param splitPaymentType the split type ("custom" / "equal")
     * @param account          the account searching for a pending request
     * @return the found SplitPaymentState or null if none
     */
    public SplitPaymentState findPendingSplit(String splitPaymentType, Account account) {
        for (SplitPaymentState s : pendingSplits) {
            if (!s.isRejected() && !s.isFinalized()
                    && s.getSplitPaymentType().equalsIgnoreCase(splitPaymentType)
                    && s.getAllAccounts().contains(account)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Marks the given account as accepting the split.
     * If all participants have accepted, the payment is finalized.
     */
    public void acceptSplit(SplitPaymentState state, Account account) {
        // Mark acceptance
        state.accept(account);

        // If all have accepted, finalize the payment
        if (state.isFullyAccepted()) {
            finalizeSplit(state);
        }
    }

    /**
     * Marks the split as rejected and removes it from the pending list.
     */
    public void rejectSplit(SplitPaymentState state) {
        state.reject();
        // Cancel any pending operations
        cancelPendingOperation(state);
        // Remove it from the list
        pendingSplits.remove(state);
    }

    /**
     * Finalizes the split payment:
     *  - Checks each account's balance
     *  - If any account is short of funds, throw "INSUFFICIENT_FUNDS:IBAN"
     *  - Otherwise, subtract amounts and move the operation from "pending" to "completed"
     *  - Remove the split request from the list
     */
    private void finalizeSplit(SplitPaymentState state) {
        // 1. Check balances
        state.getAllAccounts().forEach(acc -> {
            double amount = state.getSplitMap().get(acc);
            if (acc.getBalance() < amount) {
                throw new RuntimeException("INSUFFICIENT_FUNDS:" + acc.getIban());
            }
        });

        // 2. All good, subtract amounts and update each account's history
        state.getAllAccounts().forEach(acc -> {
            double amount = state.getSplitMap().get(acc);
            acc.removeFunds(amount);

            // Remove from pending operations, add to completed
            acc.removePendingOperation(state.getPendingOperation());
            acc.addOperation(state.getPendingOperation());
        });

        // 3. Mark as finalized
        state.setFinalized(true);

        // 4. Remove from manager's pending list
        pendingSplits.remove(state);
    }

    /**
     * Cancels the pending operation in all participant accounts if the split is rejected.
     */
    private void cancelPendingOperation(SplitPaymentState state) {
        state.getAllAccounts().forEach(acc ->
                acc.removePendingOperation(state.getPendingOperation())
        );
    }
}
