package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.cashbackStrategy.CashbackStrategy;
import org.poo.cashbackStrategy.NrOfTransactionsStrategy;
import org.poo.cashbackStrategy.SpendingThresholdStrategy;
import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Card;
import org.poo.data.Commerciant;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.CardPaymentOperation;
import org.poo.operationTypes.CreateCardOperation;
import org.poo.operationTypes.DeleteCardOperation;
import org.poo.operationTypes.FailOperation;
import org.poo.operationTypes.InfoOperation;

import java.util.List;

public class PayOnlineCommand implements Command {

    private final ObjectMapper objectMapper;
    private final ArrayNode output;
    private final ExchangeRateManager exchangeRateManager;

    public PayOnlineCommand(final ObjectMapper objectMapper, final ArrayNode output,
                            final ExchangeRateManager exchangeRateManager) {
        this.objectMapper = objectMapper;
        this.output = output;
        this.exchangeRateManager = exchangeRateManager;
    }

    @Override
    public void execute(final List<User> users, final CommandInput command) {
        User payingUser = null;

        // Find the user
        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                payingUser = user;
                break;
            }
        }

        boolean cardFound = false;

        if (payingUser != null) {
            for (Account account : payingUser.getAccounts()) {
                Card card = account.getCard(command.getCardNumber());
                if (card != null) {
                    // Check if the card is already frozen
                    if (card.isFrozen()) {
                        FailOperation frozenOperation = new FailOperation(
                                command.getTimestamp(),
                                "The card is frozen"
                        );
                        account.addOperation(frozenOperation);
                        cardFound = true;
                        break;
                    }

                    // Convert the amount if currencies are different
                    double convertedAmount = command.getAmount();
                    if (!account.getCurrency().equals(command.getCurrency())) {
                        double exchangeRate = exchangeRateManager.getExchangeRate(
                                command.getCurrency(),
                                account.getCurrency()
                        );

                        if (exchangeRate != -1) {
                            convertedAmount *= exchangeRate;
                        } else {
                            // If exchange rate not found
                            break;
                        }
                    }

                    // 1. Check for insufficient funds
                    if (account.getBalance() < convertedAmount) {
                        FailOperation insufficientFundsOperation = new FailOperation(
                                command.getTimestamp(),
                                "Insufficient funds"
                        );
                        account.addOperation(insufficientFundsOperation);
                        cardFound = true;
                        break; // Stop execution
                    }

                    // 2. Check if balance drops below the minimum balance
                    if (account.getBalance() - convertedAmount <= account.getMinBalance()) {
                        card.freeze(); // Freeze the card
                        FailOperation freezeOperation = new FailOperation(
                                command.getTimestamp(),
                                "The card is frozen"
                        );
                        account.addOperation(freezeOperation);
                        cardFound = true;
                        break; // Stop execution
                    }

                    // Cashback logic
                    double cashback = 0.0;
                    Commerciant commerciant = Commerciant.getMerchantByName(command.getCommerciant());
                    if (commerciant == null) {
                        ObjectNode errorOutput = objectMapper.createObjectNode();
                        errorOutput.put("description", "Merchant not found");
                        errorOutput.put("timestamp", command.getTimestamp());
                        output.add(errorOutput);
                        return;
                    }

                    // Apply the correct cashback strategy
                    CashbackStrategy cashbackStrategy;
                    if (commerciant.getCashbackType().equals("nrOfTransactions")) {
                        cashbackStrategy = new NrOfTransactionsStrategy();
                        cashback = cashbackStrategy.calculateCashback(
                                command.getAmount(),
                                commerciant.getCategory(),
                                commerciant.incrementAndGetTransactionCount(account),
                                0.0
                        );
                    } else if (commerciant.getCashbackType().equals("spendingThreshold")) {
                        cashbackStrategy = new SpendingThresholdStrategy();
                        double totalSpending = account.getMerchantSpending(commerciant.getName());
                        cashback = cashbackStrategy.calculateCashback(
                                command.getAmount(),
                                commerciant.getCategory(),
                                0,
                                totalSpending + command.getAmount()
                        );

                        // Convert cashback to the account's currency
                        if (!command.getCurrency().equals(account.getCurrency())) {
                            double cashbackExchangeRate = exchangeRateManager.getExchangeRate(
                                    command.getCurrency(), // Original transaction currency
                                    account.getCurrency()  // Account currency
                            );

                            if (cashbackExchangeRate != -1) {
                                cashback *= cashbackExchangeRate; // Apply exchange rate to cashback
                            } else {
                                cashback = 0.0; // If exchange rate is not found, no cashback
                            }
                        }

                        account.addMerchantSpending(commerciant.getName(), command.getAmount());
                    }

                    // Apply cashback and perform the payment
                    double finalAmount = convertedAmount - cashback;
                    account.removeFunds(finalAmount);

                    // Add payment operation
                    CardPaymentOperation paymentOperation = new CardPaymentOperation(
                            command.getTimestamp(),
                            account.getIban(),
                            command.getCardNumber(),
                            command.getCommerciant(),
                            finalAmount + cashback, // I need the original amount for the operation
                            account.getCurrency(),
                            "Card payment"
                    );
                    account.addOperation(paymentOperation);

                    // One-time card logic
                    if (card.getCardType().equals("OneTime")) {
                        String oldCardNr = card.getCardNumber();
                        DeleteCardOperation destroyOneTime = new DeleteCardOperation(
                                command.getTimestamp(),
                                account.getIban(),
                                oldCardNr,
                                payingUser.getEmail(),
                                "The card has been destroyed"
                        );
                        account.addOperation(destroyOneTime);

                        CreateCardOperation newOneTime = new CreateCardOperation(
                                command.getTimestamp(),
                                account.getIban(),
                                card.getCardNumber(),
                                payingUser.getEmail(),
                                "New card created"
                        );
                        account.addOperation(newOneTime);
                    }

                    cardFound = true;
                    break;
                }
            }
        }

        if (!cardFound) {
            ObjectNode payOnlineOutput = objectMapper.createObjectNode();
            ObjectNode outputDetails = objectMapper.createObjectNode();

            payOnlineOutput.put("command", "payOnline");
            outputDetails.put("description", "Card not found");
            outputDetails.put("timestamp", command.getTimestamp());
            payOnlineOutput.set("output", outputDetails);
            payOnlineOutput.put("timestamp", command.getTimestamp());

            output.add(payOnlineOutput);
        }
    }
}
