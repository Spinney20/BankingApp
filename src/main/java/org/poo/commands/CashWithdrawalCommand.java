package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Card;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.FailOperation;
import org.poo.operationTypes.InfoOperation;

import java.sql.SQLOutput;
import java.util.List;

public class CashWithdrawalCommand implements Command {

    private final ExchangeRateManager exchangeRateManager;
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public CashWithdrawalCommand(final ExchangeRateManager exchangeRateManager, final ObjectMapper objectMapper, final ArrayNode output) {
        this.exchangeRateManager = exchangeRateManager;
        this.objectMapper = objectMapper;
        this.output = output;
    }

    @Override
    public void execute(final List<User> users, final CommandInput command) {
        String cardNumber = command.getCardNumber();
        String userEmail = command.getEmail();
        double withdrawalAmountRON = command.getAmount(); // Withdrawal amount is in RON
        String location = command.getLocation();

        // Find user and account
        User withdrawingUser = null;
        Card withdrawingCard = null;
        Account linkedAccount = null;

        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(userEmail)) {
                withdrawingUser = user;
                for (Account account : user.getAccounts()) {
                    for (Card card : account.getCards()) {
                        if (card.getCardNumber().equals(cardNumber)) {
                            withdrawingCard = card;
                            linkedAccount = account;
                            break;
                        }
                    }
                    if (withdrawingCard != null) break;
                }
                break;
            }
        }

        if (withdrawingUser == null) {
            // JSON output for "User not found"
            addOutputToJson("User not found", command.getTimestamp());
            return;
        }

        if (withdrawingCard == null) {
            // JSON output for "Card not found"
            addOutputToJson("Card not found", command.getTimestamp());
            System.out.println("Card not found, timestamp: " + command.getTimestamp());
            return;
        }

        // Calculate commission
        double commissionRON = withdrawingUser.applyCommission(withdrawalAmountRON);

        // Total amount in RON
        double totalAmountRON = withdrawalAmountRON + commissionRON;

        // Convert total amount to the account's currency if necessary
        double totalAmountInAccountCurrency = totalAmountRON;
        if (!linkedAccount.getCurrency().equalsIgnoreCase("RON")) {
            double exchangeRate = exchangeRateManager.getExchangeRate("RON", linkedAccount.getCurrency());
            if (exchangeRate != -1) {
                totalAmountInAccountCurrency = totalAmountRON * exchangeRate;
            } else {
                addFailureToAccount(linkedAccount, command, "Exchange rate not available");
                return;
            }
        }

        // Check if the account has sufficient funds
        if (linkedAccount.getBalance() < totalAmountInAccountCurrency) {
            addFailureToAccount(linkedAccount, command, "Insufficient funds");
            return;
        }

        // Check minimum balance constraint
        if (linkedAccount.getBalance() - totalAmountInAccountCurrency < linkedAccount.getMinBalance()) {
            addFailureToAccount(linkedAccount, command, "Cannot perform payment due to a minimum balance being set");
            return;
        }

        // Deduct funds and mark card as used
        linkedAccount.removeFunds(totalAmountInAccountCurrency);

        // Add cash withdrawal operation to the account
        InfoOperation withdrawalOperation = new InfoOperation(
                command.getTimestamp(),
                "Cash withdrawal of " + String.format("%.2f", withdrawalAmountRON) + " RON (Commission: " +
                        String.format("%.2f", commissionRON) + " RON) from " + location
        );
        linkedAccount.addOperation(withdrawalOperation);
    }

    /**
     * Adds a failure operation to the account.
     */
    private void addFailureToAccount(Account account, CommandInput command, String description) {
        if (account != null) {
            FailOperation failOperation = new FailOperation(
                    command.getTimestamp(),
                    description
            );
            account.addOperation(failOperation);
        }
    }

    /**
     * Adds an output error to the JSON output array.
     */
    private void addOutputToJson(String description, int timestamp) {
        ObjectNode outputNode = objectMapper.createObjectNode();
        outputNode.put("command", "cashWithdrawal");
        ObjectNode outputDetails = objectMapper.createObjectNode();
        outputDetails.put("description", description);
        outputDetails.put("timestamp", timestamp);
        outputNode.set("output", outputDetails);
        outputNode.put("timestamp", timestamp);
        output.add(outputNode);
    }
}
