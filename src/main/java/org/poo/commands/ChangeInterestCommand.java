package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.InfoOperation;

import java.util.List;

public class ChangeInterestCommand implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public ChangeInterestCommand(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Changes the interest rate of a savings account
     * If the account doesn't exist, an error is added to the output
     * If the account is not a savings account, an error is added to the output
     * If the account is a savings account, the interest rate is changed
     * with the method setInterestRate
     * @param users list of users
     * @param command the command to be executed
     */
    @Override
    public void execute(final List<User> users, final CommandInput command) {
        Account targetAccount = null;

        // Finding the acc
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    targetAccount = account;
                    break;
                }
            }
            if (targetAccount != null) {
                break; // stop the search if we found the account
            }
        }

        // Error if it doesn't exist
        if (targetAccount == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("command", "changeInterestRate");
            ObjectNode outputNode = objectMapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", command.getTimestamp());
            errorResponse.set("output", outputNode);
            errorResponse.put("timestamp", command.getTimestamp());
            output.add(errorResponse);
            return;
        }

        // Error if it's not a savings account
        if (!targetAccount.getAccountType().equals("savings")) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("command", "changeInterestRate");
            ObjectNode outputNode = objectMapper.createObjectNode();
            outputNode.put("description", "This is not a savings account");
            outputNode.put("timestamp", command.getTimestamp());
            errorResponse.set("output", outputNode);
            errorResponse.put("timestamp", command.getTimestamp());
            output.add(errorResponse);
            return;
        }

        // Changing the rate
        targetAccount.setInterestRate(command.getInterestRate());

        InfoOperation changeInterest = new InfoOperation(
                command.getTimestamp(),
                "Interest rate of the account changed to " + command.getInterestRate()
        );
        targetAccount.addOperation(changeInterest);
    }
}
