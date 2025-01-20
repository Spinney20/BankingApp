package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.splitManager.SplitPaymentManager;
import org.poo.splitManager.SplitPaymentState;

import java.util.List;

/**
 * Handles "rejectSplitPayment":
 *  1) Find the user by email
 *  2) Find the first pending split of the given type (if any) for one of the user's accounts
 *  3) Mark reject => remove from pending => "One user rejected the payment."
 */
public class RejectSplitPaymentCommand implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public RejectSplitPaymentCommand(ObjectMapper objectMapper, ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    @Override
    public void execute(List<User> users, CommandInput command) {
        // 1) Find the user
        User user = findUserByEmail(users, command.getEmail());
        if (user == null) {
            generateOutput("User not found", command.getTimestamp());
            return;
        }

        // 2) Find the first pending split of "splitPaymentType" for one of this user's accounts
        String splitType = command.getSplitPaymentType();
        if (splitType == null) {
            splitType = "equal";
        }

        SplitPaymentManager manager = SplitPaymentManager.getInstance();

        SplitPaymentState state = null;
        Account userAccount = null;

        for (Account acc : user.getAccounts()) {
            SplitPaymentState candidate = manager.findPendingSplit(splitType, acc);
            if (candidate != null) {
                state = candidate;
                userAccount = acc;
                break;
            }
        }

        if (state == null || userAccount == null) {
            generateOutput("No pending split of type " + splitType + " found for user", command.getTimestamp());
            return;
        }

        // 3) Reject
        try {
            manager.rejectSplit(state);
        } catch (Exception e) {
            generateOutput(e.getMessage(), command.getTimestamp());
            return;
        }
    }

    private User findUserByEmail(List<User> users, String email) {
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }

    private void generateOutput(String message, int timestamp) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", message);
        output.add(node);
    }
}
