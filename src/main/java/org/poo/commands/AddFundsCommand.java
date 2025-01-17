package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class AddFundsCommand implements Command {

    /***
     * This method handles the command for adding funds to an account
     * It searches for the account with the given IBAN
     * And adds the amount to the account easy
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final CommandInput command) {
        // Iterate through users
        for (User user : users) {
            if (user.getAccounts() != null) {
                for (Account account : user.getAccounts()) {
                    if (account.getIban().equals(command.getAccount())) {
                        account.addFunds(command.getAmount());
                        break;
                    }
                }
            }
        }
    }
}
