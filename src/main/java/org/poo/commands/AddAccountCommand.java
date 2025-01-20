package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.factories.AccountFactory;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.AccountCreationOperation;

import java.util.List;

import static org.poo.utils.Utils.generateIBAN;

public class AddAccountCommand implements Command {

    private final ExchangeRateManager exchangeRateManager;

    /**
     * Constructor for AddAccountCommand.
     * It initializes the exchange rate manager.
     *
     * @param exchangeRateManager The exchange rate manager used for currency conversion.
     */
    public AddAccountCommand(ExchangeRateManager exchangeRateManager) {
        this.exchangeRateManager = exchangeRateManager;
    }

    /**
     * This method handles the command for adding an account to a user.
     * It:
     * - Searches for the user with the given email.
     * - Generates an IBAN for the new account.
     * - Creates the account using the AccountFactory.
     * - Adds the account to the user.
     * - Creates an account creation operation and attaches it to the account.
     *
     * @param users   - List of users in the system.
     * @param command - The command input containing account details.
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants, final CommandInput command) {

        for (User user : users) { // Search for the user
            if (user.getEmail().equals(command.getEmail())) {
                String iban = generateIBAN(); // Generate IBAN with the utility method

                // Create the account using the factory
                Account account = AccountFactory.createAccount(
                        command.getAccountType(),
                        iban,
                        command.getCurrency(),
                        user.getEmail(), // Pass user email as ownerEmail for BusinessAccount
                        command.getInterestRate(), // Interest rate is used for SavingsAccount
                        exchangeRateManager // Pass ExchangeRateManager
                );

                user.addAccount(account); // Add the created account to the user

                // Creating the account has to be added as an operation
                AccountCreationOperation creationOperation = new AccountCreationOperation(
                        command.getTimestamp(),
                        "New account created"
                );
                account.addOperation(creationOperation);

                break; // Exit the loop once the account is added
            }
        }
    }
}
