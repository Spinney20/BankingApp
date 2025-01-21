package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.accountTypes.BusinessAccount;
import org.poo.data.Commerciant;
import org.poo.operationTypes.FailOperation;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class AddFundsCommand implements Command {

    /**
     * This method handles the command for adding funds to an account.
     * 1) We find the user whose email = command.getEmail().
     * 2) We check if that user has the account with IBAN = command.getAccount().
     * 3) If it's a business account and the user is an employee, compare the deposit limit.
     * 4) If limit not exceeded, we add funds and add a deposit operation.
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants,
                        final CommandInput command) {
        // 1) Find the user by email
        User depositUser = null;
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(command.getEmail())) {
                depositUser = user;
                break;
            }
        }

        // If no user with that email was found
        if (depositUser == null) {
            // No user found => Do nothing or log it
            return;
        }

        // 2) Find the account in depositUser's account list
        Account targetAccount = null;
        for (Account acc : depositUser.getAccounts()) {
            if (acc.getIban().equals(command.getAccount())) {
                targetAccount = acc;
                break;
            }
        }

        // If the user doesn't own/associate with that account => no deposit
        if (targetAccount == null) {
            // We could log or fail: "User does not have this account."
            return;
        }

        // 3) If it's a business account, check if depositUser is an employee => deposit limit
        if (targetAccount.isBusinessAccount()) {
            BusinessAccount bAcc = (BusinessAccount) targetAccount;
            if (bAcc.isEmployee(depositUser)) {
                // Compare command.getAmount() with bAcc.getGlobalDepositLimit()
                double depositAmount = command.getAmount();
                double depositLimit = bAcc.getGlobalDepositLimit();

                System.out.println("Checking isEmployee for userEmail=" + depositUser.getEmail());
                System.out.println("Role is employee, deposit amount=" + depositAmount);
                System.out.println("Deposit limit=" + depositLimit);

                if (depositAmount > depositLimit) {
                    // Exceed deposit limit => fail
                    FailOperation depositLimitFail = new FailOperation(
                            command.getTimestamp(),
                            "Employee deposit limit exceeded"
                    );
                    bAcc.addOperation(depositLimitFail);
                    return; // Stop execution
                }
            }
        }

        // 4) If limit not exceeded (or not business account), proceed
        targetAccount.addFunds(command.getAmount());
        targetAccount.addDeposit(depositUser.getEmail(), command.getAmount());
    }
}
