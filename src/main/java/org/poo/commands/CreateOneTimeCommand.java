package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.CreateCardOperation;

import java.util.List;

import static org.poo.utils.Utils.generateCardNumber;

public class CreateOneTimeCommand implements Command {
    /***
     * Handles the createOneTime command
     * Searches for the specified account and user
     * Generates a new card number with the generateCardNumber method
     * Creates a new card for the specified account
     * Adds an operation for the card creation
     * Just like the CreateCardCommand, but with a different card type
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants,
                        final CommandInput command) {
        // Iterate through users
        for (User user : users) {
            if (user.getAccounts() != null) {
                for (Account account : user.getAccounts()) {
                    if (account.getIban().equals(command.getAccount())) {
                        String cardNumber = generateCardNumber();
                        account.addCard("onetime", cardNumber);

                        // Create an operation for the newly created card
                        CreateCardOperation cardCreationOperation = new CreateCardOperation(
                                command.getTimestamp(),
                                account.getIban(),
                                cardNumber,
                                user.getEmail(),
                                "New card created"
                        );
                        account.addOperation(cardCreationOperation);
                        break;
                    }
                }
            }
        }
    }
}
