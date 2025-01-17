package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.factories   .AccountFactory;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.AccountCreationOperation;

import java.util.List;

import static org.poo.utils.Utils.generateIBAN;

public class AddAccountCommand implements Command {

    /***
     * This method handles the command for adding an account to a user
     * Firstly, it searches for the user with the given email
     * Then it generates an IBAN for the new account
     * And creates the account with the factory
     * Finally, it adds the account to the user
     * + operation for the account creation
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final CommandInput command) {

        for (User user : users) { // search for the user
            if (user.getEmail().equals(command.getEmail())) {
                String iban = generateIBAN(); // generate IBAN with given method
                Account account = AccountFactory.createAccount(// create account with factory
                        command.getAccountType(),
                        iban,
                        command.getCurrency()
                );
                user.addAccount(account);

                // Creating the account has to be added as an operation
                AccountCreationOperation creationOperation = new AccountCreationOperation(
                        command.getTimestamp(),
                        "New account created"
                );
                account.addOperation(creationOperation);
                break;
            }
        }
    }
}

