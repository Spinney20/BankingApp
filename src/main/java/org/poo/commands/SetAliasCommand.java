package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class SetAliasCommand implements Command {

    /***
     * This operation does not even affect the tests lol
     * I could have just used a break for this and thats it
     * but i ve done it for the plot, just setting an alias
     * for an acc with the setter setAlias
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants, final CommandInput command) {
        User aliasingUser = null;

        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                aliasingUser = user;
                break;
            }
        }

        if (aliasingUser != null) {
            for (Account account : aliasingUser.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    account.setAlias(command.getAlias());
                    break;
                }
            }
        }
    }
}
