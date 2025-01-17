package org.poo.commandPattern;

import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

/***
 * Interface for the commands
 * Every command uses the users and the command input
 * The command input is the input from the file
 * The users are the list of users
 * Every command uses this interface
 */
public interface Command {
    /***
     * I only need these 2 parameters that are used in every command class
     * @param users - list of users
     * @param command - the command to be executed
     */
    void execute(List<User> users, CommandInput command);
}
