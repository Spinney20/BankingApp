package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.AddInterestOperation;

import java.util.List;

public class AddInterestCommand implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public AddInterestCommand(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * This method handles the command for adding interest to a savings account
     * It searches for the account with the given IBAN
     * If the account is not found, an error is added to the output ofc cause
     * this its what the ref makes me do
     * If the account is not a savings account, an error is added to the output
     * If the account is a savings account, the interest is calculated and added to the account
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants, final CommandInput command) {
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
                break; // it means we found the account stop the search
            }
        }

        // Error if it doesn't exist
        if (targetAccount == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("command", "addInterest");
            ObjectNode outputNode = objectMapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", command.getTimestamp());
            errorResponse.set("output", outputNode);
            errorResponse.put("timestamp", command.getTimestamp());
            output.add(errorResponse);
            return;
        }

        // Error if its not savings
        if (!targetAccount.getAccountType().equals("savings")) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("command", "addInterest");
            ObjectNode outputNode = objectMapper.createObjectNode();
            outputNode.put("description", "This is not a savings account");
            outputNode.put("timestamp", command.getTimestamp());
            errorResponse.set("output", outputNode);
            errorResponse.put("timestamp", command.getTimestamp());
            output.add(errorResponse);
            return;
        }

        // Calculating and then adding the interest
        double interest = targetAccount.calculateInterest();
        interest *= 100;
        targetAccount.addFunds(interest);

        // Adding the operation to the account
        AddInterestOperation interestOperation = new AddInterestOperation(
                command.getTimestamp(),
                interest,
                targetAccount.getCurrency()
        );
        targetAccount.addOperation(interestOperation);
    }
}
