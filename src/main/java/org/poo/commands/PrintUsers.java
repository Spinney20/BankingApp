package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Card;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class PrintUsers implements Command {

    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    // I need the ObjectMapper and the output ArrayNode too for this one
    public PrintUsers(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Print user command nothing to complicated
     * I find each user and then I print all the accounts and cards
     * just like in the ref cause I didnt have an example xD
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants, final CommandInput command) {
        ObjectNode commandOutput = objectMapper.createObjectNode();
        ArrayNode usersOutput = objectMapper.createArrayNode();

        for (User user : users) {
            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("firstName", user.getFirstName());
            userNode.put("lastName", user.getLastName());
            userNode.put("email", user.getEmail());

            ArrayNode accountsNode = objectMapper.createArrayNode();
            if (user.getAccounts() != null) {
                for (Account account : user.getAccounts()) {
                    ObjectNode accountNode = objectMapper.createObjectNode();
                    accountNode.put("IBAN", account.getIban());
                    accountNode.put("balance", account.getBalance());
                    accountNode.put("currency", account.getCurrency());
                    accountNode.put("type", account.getAccountType());

                    ArrayNode cardsNode = objectMapper.createArrayNode();
                    for (Card card : account.getCards()) {
                        ObjectNode cardNode = objectMapper.createObjectNode();
                        cardNode.put("cardNumber", card.getCardNumber());
                        cardNode.put("status", card.getStatus());
                        cardsNode.add(cardNode);
                    }

                    accountNode.set("cards", cardsNode);
                    accountsNode.add(accountNode);
                }
            }
            userNode.set("accounts", accountsNode);
            usersOutput.add(userNode);
        }

        commandOutput.put("command", "printUsers");
        commandOutput.set("output", usersOutput);
        commandOutput.put("timestamp", command.getTimestamp());
        output.add(commandOutput);
    }
}
