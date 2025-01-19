package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.accountTypes.BusinessAccount;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class ChangeSpendingLimitCommand implements Command {

    @Override
    public void execute(List<User> users, CommandInput command) {
        String accountIban = command.getAccount();
        String requesterEmail = command.getEmail();
        double newSpendingLimit = command.getAmount();

        // Find the account by IBAN
        Account account = findAccountByIban(accountIban, users);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        // Ensure the account is a business account
        if (!(account instanceof BusinessAccount)) {
            System.out.println("Account is not of type business.");
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;

        // Check if the requester is the owner
        if (!businessAccount.isOwner(requesterEmail)) {
            System.out.println("You are not authorized to make this transaction.");
            return;
        }

        // Update the spending limit
        businessAccount.changeGlobalSpendingLimit(newSpendingLimit);
        System.out.println("Spending limit successfully updated to " + newSpendingLimit);
    }

    private Account findAccountByIban(String iban, List<User> users) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return account;
                }
            }
        }
        return null;
    }
}
