package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.DeleteCardOperation;

import java.util.List;

public class DeleteCardCommand implements Command {

    /***
     * Handles the deleteCard command
     * Searches for the specified card and user
     * Deletes the card if it exists
     * If the card is not found, it will add an error to the account operations
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants,
                        final CommandInput command) {
        // Iterate through users
        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                // Searching in all the accounts
                // because you didn’t give me the IBAN in JSON :(
                for (Account account : user.getAccounts()) {
                    try {
                        account.deleteCard(command.getCardNumber());

                        // Create and add the DeleteCardOperation
                        DeleteCardOperation deleteCardOperation = new DeleteCardOperation(
                                command.getTimestamp(),
                                account.getIban(),
                                command.getCardNumber(),
                                user.getEmail(),
                                "The card has been destroyed"
                        );
                        account.addOperation(deleteCardOperation);

                        break;
                    } catch (IllegalArgumentException e) {
                        // If exception, continue
                        // (exception is -> card not found in account)
                    }
                }
                break;
            }
        }
    }
}

