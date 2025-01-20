package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class SetMinimumBalance implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public SetMinimumBalance(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Setting a minimum balance for an account
     * with the setter created in acc
     * of course if the acc not existent i put an error
     * on the output
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants, final CommandInput command) {
        Account targetAccount = null;

        // finding the acc
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    targetAccount = account;
                    break;
                }
            }
            if (targetAccount != null) {
                break;
            }
        }

        // nonexistent acc = error
        if (targetAccount == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("command", "setMinimumBalance");
            errorResponse.put("description", "Account not found");
            errorResponse.put("timestamp", command.getTimestamp());
            output.add(errorResponse);
            return;
        }

        // Setting the min balance
        targetAccount.setMinBalance(command.getAmount());
    }
}
