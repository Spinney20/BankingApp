package org.poo.commandPattern;

import java.util.List;

import org.poo.data.User;
import org.poo.fileio.CommandInput;

public class CommandInvoker {
    private final CommandFactory commandFactory;

    public CommandInvoker(final CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /***
     * Executes the command, nothing complicated
     * @param commandType the type of the command
     * @param users - the list of users
     * @param command - the command input
     */
    public void executeCommand(final String commandType, final List<User> users,
                               final CommandInput command) {
        Command cmd = commandFactory.createCommand(commandType);
        if(cmd == null) {
            return;
        }
        cmd.execute(users, command);
    }
}
