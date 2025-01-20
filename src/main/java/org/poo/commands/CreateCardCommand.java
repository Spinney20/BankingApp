package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.CreateCardOperation;

import java.util.List;

import static org.poo.utils.Utils.generateCardNumber;

public class CreateCardCommand implements Command {

    /***
     * Handles the createCard command
     * Searches for the specified account and user
     * Generates a new card number with the generateCardNumber method
     * Creates a new card for the specified account
     * Adds an operation for the card creation
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants, final CommandInput command) {
        boolean cardCreated = false;

        // Iterate through users
        for (User user : users) {
            // Check if the email matches
            if (user.getEmail().equals(command.getEmail())) {
                for (Account account : user.getAccounts()) {
                    // Check if the account IBAN matches
                    if (account.getIban().equals(command.getAccount())) {
                        // Generate the card number and add it to the account
                        String cardNumber = generateCardNumber();
                        account.addCard("regular", cardNumber);

                        // Create an operation for the newly created card
                        CreateCardOperation cardCreationOperation = new CreateCardOperation(
                                command.getTimestamp(),
                                account.getIban(),
                                cardNumber,
                                user.getEmail(),
                                "New card created"
                        );
                        account.addOperation(cardCreationOperation);

                        cardCreated = true; // Mark the card as created
                        break;
                    }
                }
            }
            if (cardCreated) {
                break; // Exit the loops if the card has been created
            }
        }

        // If the account or user is not found, no action is performed
    }
}

