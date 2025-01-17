package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.FailOperation;

import java.util.List;

public class DeleteAccountCommand implements Command {

    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    // Constructor because i need objectMapper and output for this one
    public DeleteAccountCommand(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Handles the deleteAccount command
     * Deletes the account if it exists
     * If the account has funds, it will add an error to the account operations
     * If the user is not found, it will add an error to the output
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final CommandInput command) {
        ObjectNode deleteAccountOutput = objectMapper.createObjectNode();
        ObjectNode outputDetails = objectMapper.createObjectNode();

        deleteAccountOutput.put("command", "deleteAccount");
        deleteAccountOutput.put("timestamp", command.getTimestamp());

        // Searching by email
        User targetUser = null;
        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                targetUser = user;
                break;
            }
        }

        Account toBeDeletedAccount = null;

        // Finding the acc to be deleted
        if (targetUser != null) {
            for (Account account : targetUser.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    toBeDeletedAccount = account;
                    break;
                }
            }
        }

        if (targetUser == null) {
            // If user not found
            outputDetails.put("error", "User not found");
            outputDetails.put("timestamp", command.getTimestamp());
            deleteAccountOutput.set("output", outputDetails);
            output.add(deleteAccountOutput);
            return;
        }

        try {
            // Trying to delete
            targetUser.deleteAccount(command.getAccount());
            outputDetails.put("success", "Account deleted");
        } catch (IllegalArgumentException e) {
            // Catching the exception thrown by deleteAccount
            FailOperation cantDeleteOperation = new FailOperation(
                    command.getTimestamp(),
                    "Account couldn't be deleted - there are funds remaining"
            );
            toBeDeletedAccount.addOperation(cantDeleteOperation);
            outputDetails.put("error", e.getMessage());
        }

        // The usual
        outputDetails.put("timestamp", command.getTimestamp());
        deleteAccountOutput.set("output", outputDetails);
        output.add(deleteAccountOutput);
    }
}
