package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.accountTypes.BusinessAccount;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class ChangeDepositLimitCommand implements Command {

    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public ChangeDepositLimitCommand(ObjectMapper objectMapper, ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    @Override
    public void execute(List<User> users, CommandInput command) {
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
            addErrorToOutput("Account is not of type business.", command.getTimestamp());
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;

        // Check if the requester is the owner
        if (!businessAccount.isOwner(requesterEmail)) {
            addErrorToOutput("You must be owner in order to change deposit limit.", command.getTimestamp());
            return;
        }

        // Update the deposit limit
        businessAccount.changeGlobalDepositLimit(newDepositLimit);
    }

    private Account findAccountByIban(String iban, List<User> users) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return account;
                }
            }
        }
        return null;
    }

    private void addErrorToOutput(String description, int timestamp) {
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
