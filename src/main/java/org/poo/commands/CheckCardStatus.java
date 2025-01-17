package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Card;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.CheckCardStatusOperation;

import java.util.List;

public class CheckCardStatus implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public CheckCardStatus(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Handles the checkCardStatus command
     * Checks if the card exists and if it is frozen
     * If the account balance is below the minimum balance, a warning is added
     * to the account operations
     * Also errors for card not found are added to the output
     * @param users list of users
     * @param command the command to be executed
     */
    @Override
    public void execute(final List<User> users, final CommandInput command) {
        boolean cardExists = false;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                Card card = account.getCard(command.getCardNumber());
                if (card != null) {
                    cardExists = true;

                    if (card.isFrozen()) {
                        break;
                    } else if (account.getBalance() <= account.getMinBalance()) {
                        // Warning -> add operation
                        CheckCardStatusOperation warningOperation = new CheckCardStatusOperation(
                                command.getTimestamp(),
                                "You have reached the minimum amount of funds, "
                                        + "the card will be frozen"
                        );
                        account.addOperation(warningOperation);
                    }
                    break;
                }
            }
        }

        if (!cardExists) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            ObjectNode outputDetails = objectMapper.createObjectNode();

            errorResponse.put("command", "checkCardStatus");
            outputDetails.put("description", "Card not found");
            outputDetails.put("timestamp", command.getTimestamp());
            errorResponse.set("output", outputDetails);
            errorResponse.put("timestamp", command.getTimestamp());

            output.add(errorResponse);
        }
    }
}
