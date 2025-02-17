package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.splitManager.SplitPaymentManager;
import org.poo.splitManager.SplitPaymentState;

import java.util.List;

/**
 * Handles "acceptSplitPayment":
 *  1) Find the user by email
 *  2) Find the first pending split of the given type (if any) for one of the user's accounts
 *  3) Mark accept, if all participants accept => finalize
 *  4) If a user's account has insufficient funds => throw "INSUFFICIENT_FUNDS: IBAN"
 *     => catch in this command => print "Account IBAN has insufficient funds for a split payment."
 */
public class AcceptSplitPaymentCommand implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public AcceptSplitPaymentCommand(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Accepts a split payment for a user.
     * this is handling a lot of things
     * like making sure that all the users accepted and if yes
     * grabbing the pending transaction and finalizing it
     * @param users - list of users
     * @param commerciants
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants,
                        final CommandInput command) {
        // 1) Find the user
        User user = findUserByEmail(users, command.getEmail());
        if (user == null) {
            generateOutput("User not found", command.getTimestamp());
            return;
        }
        //2) Find the first pending split of "splitPaymentType" for one of this user's accounts
        String splitType = command.getSplitPaymentType();
        if (splitType == null) {
            splitType = "equal"; // default if not specified
        }

        SplitPaymentManager manager = SplitPaymentManager.getInstance();

        SplitPaymentState state = null;
        Account userAccount = null;

        // Loop through the user's accounts to find a match
        for (Account acc : user.getAccounts()) {
            SplitPaymentState candidate = manager.findPendingSplit(splitType, acc);
            if (candidate != null) {
                state = candidate;
                userAccount = acc;
                break;
            }
        }

        if (state == null || userAccount == null) {
            generateOutput("No pending split of type " + splitType
                    + " found for user", command.getTimestamp());
            return;
        }

        // 3) Accept the split
        try {
            manager.acceptSplit(state, userAccount);
            // If all accounts accept,
            // it will finalize => might throw "INSUFFICIENT_FUNDS" if short on balance
        } catch (RuntimeException e) {
            if (e.getMessage().startsWith("INSUFFICIENT_FUNDS")) {
                String iban = e.getMessage().split(":")[1];
                generateOutput("Account " + iban
                                + " has insufficient funds for a split payment.",
                        command.getTimestamp());
            } else {
                generateOutput(e.getMessage(), command.getTimestamp());
            }
        }
    }

    private User findUserByEmail(final List<User> users, final String email) {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    private void generateOutput(final String message, final int timestamp) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", message);
        output.add(node);
    }
}
