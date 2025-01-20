package org.poo.splitManager;

import org.poo.data.Account;
import org.poo.data.Operation;
import org.poo.operationTypes.SplitCustomPaymentOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * Global manager for split payments:
 *  - Maintains a list of active "pending" split payment requests
 *  - At acceptance/rejection, it searches the first matching request
 *  - Finalizes the payment when all participants accept
 *  - Cancels it if any user rejects
 *  - If insufficient funds, it sets the error on the operation and rejects
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
     * Adds a new split payment state to the list of pending splits.
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
     * If all participants have accepted, the payment is finalized (or tries to finalize).
     */
    public void acceptSplit(SplitPaymentState state, Account account) {
        state.accept(account);
        if (state.isFullyAccepted()) {
            finalizeSplit(state);
        }
    }

    /**
     * Marks the split as rejected and removes it from the pending list.
     */
    public void rejectSplit(SplitPaymentState state) {
        String msg = "One user rejected the payment.";

        Operation op = state.getPendingOperation();
        op.setError(msg);
        state.reject();
        for (Account acc : state.getAllAccounts()) {
            acc.removePendingOperation(op);
            acc.addOperation(op);
        }
        pendingSplits.remove(state);
    }

    /**
     * Finalizes the split payment:
     *  - Checks each account's balance
     *  - If any account has insufficient funds, we set an error on the operation,
     *    reject the split, and remove from pending
     *  - Otherwise, subtract amounts and move the operation from "pending" to "completed"
     *  - Remove the split request from the list
     */
    private void finalizeSplit(SplitPaymentState state) {
        // 1) Check if any account has insufficient funds
        Account insufficientAccount = null;
        for (Account acc : state.getAllAccounts()) {
            double amount = state.getSplitMap().get(acc);
            if (acc.getBalance() < amount) {
                insufficientAccount = acc;
                break;
            }
        }

        if (insufficientAccount != null) {
            // We found an account with not enough balance
            String msg = "Account " + insufficientAccount.getIban()
                    + " has insufficient funds for a split payment.";

            Operation op = state.getPendingOperation();
            // Instead of instanceof, we rely on op.setError(...)
            // which is overridden in SplitCustomPaymentOperation,
            // and does nothing for plain ones if you prefer that.
            op.setError(msg);

            // Mark the state as rejected
            state.reject();
            pendingSplits.remove(state);

            // Remove from pending in each account and add to final history so the user sees the error
            for (Account acc : state.getAllAccounts()) {
                acc.removePendingOperation(op);
                acc.addOperation(op);
            }

            return; // stop here
        }

        // 2) If all accounts have enough funds, finalize
        for (Account acc : state.getAllAccounts()) {
            double amount = state.getSplitMap().get(acc);
            acc.removeFunds(amount);

            acc.removePendingOperation(state.getPendingOperation());
            acc.addOperation(state.getPendingOperation());
        }

        // 3) Mark as finalized
        state.setFinalized(true);

        // 4) Remove from manager
        pendingSplits.remove(state);
    }

    /**
     * Cancels the pending operation in all participant accounts if the split is rejected.
     */
    private void cancelPendingOperation(SplitPaymentState state) {
        for (Account acc : state.getAllAccounts()) {
            acc.removePendingOperation(state.getPendingOperation());
        }
    }
}
