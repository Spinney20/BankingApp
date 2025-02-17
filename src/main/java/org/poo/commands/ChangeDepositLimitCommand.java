package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.accountTypes.BusinessAccount;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class ChangeDepositLimitCommand implements Command {

    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public ChangeDepositLimitCommand(final ObjectMapper objectMapper,
                                     final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Changes the deposit limit of a business account.
     * @param users - list of users
     * @param commerciants
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants,
                        final CommandInput command) {
        String accountIban = command.getAccount();
        String requesterEmail = command.getEmail();
        double newDepositLimit = command.getAmount();

        // Find the account by IBAN
        Account account = findAccountByIban(accountIban, users);
        if (account == null) {
            addErrorToOutput("Account not found.", command.getTimestamp());
            return;
        }

        if (!account.isBusinessAccount()) {
            addErrorToOutput("Account is not of type business.",
                    command.getTimestamp());
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;

        // Check if the requester is the owner
        if (!businessAccount.isOwner(requesterEmail)) {
            addErrorToOutput("You must be owner in order to change deposit limit.",
                    command.getTimestamp());
            return;
        }

        // Update the deposit limit
        businessAccount.changeGlobalDepositLimit(newDepositLimit);
    }

    private Account findAccountByIban(final String iban, final List<User> users) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return account;
                }
            }
        }
        return null;
    }

    private void addErrorToOutput(final String description, final int timestamp) {
        ObjectNode errorNode = objectMapper.createObjectNode();
        errorNode.put("command", "changeDepositLimit");
        ObjectNode outputDetails = objectMapper.createObjectNode();
        outputDetails.put("description", description);
        outputDetails.put("timestamp", timestamp);
        errorNode.set("output", outputDetails);
        errorNode.put("timestamp", timestamp);
        output.add(errorNode);
    }
}
