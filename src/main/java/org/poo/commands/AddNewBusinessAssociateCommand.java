package org.poo.commands;

import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.accountTypes.BusinessAccount;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public class AddNewBusinessAssociateCommand implements Command {

    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants,
                        final CommandInput command) {
        String accountIban = command.getAccount();
        String newAssociateEmail = command.getEmail();
        String role = command.getRole();

        User associate = findUserByEmail(users, command.getEmail());
        if (associate == null) {
            System.out.println("User not found");
            return;
        }

        Account account = findAccountByIban(accountIban, users);
        if (account == null) {
            System.out.println("Account not found");
            return;
        }

        if (!(account instanceof BusinessAccount)) {
            System.out.println("Account is not of type business");
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;


        User newAssociate = findUserByEmail(users, newAssociateEmail);
        if (newAssociate == null) {
            System.out.println("User not found");
            return;
        }

        if (businessAccount.isAssociate(newAssociateEmail)) {
            System.out.println("The user is already an associate of the account.");
            return;
        }

        businessAccount.addAssociate(newAssociateEmail, role, associate);
        System.out.println("Associate added successfully  with reole " + role);
    }

    private User findUserByEmail(final List<User> users, final String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private Account findAccountByIban(final String iban, final List<User> users) {
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
