package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Operation;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.SplitCustomPaymentOperation;
import org.poo.operationTypes.SplitPaymentOperation;
import org.poo.splitManager.SplitPaymentManager;
import org.poo.splitManager.SplitPaymentState;
import org.poo.splitStrategy.CustomSplitPaymentStrategy;
import org.poo.splitStrategy.EqualSplitPaymentStrategy;
import org.poo.splitStrategy.SplitPaymentStrategy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the "splitPayment" command:
 *  1) Finds the involved accounts
 *  2) Chooses the splitting strategy (equal or custom)
 *  3) Builds a splitMap (Account -> amount)
 *  4) Creates a SplitPaymentOperation (pending)
 *  5) Creates a SplitPaymentState and adds it to the manager
 *  6) Adds the pending operation to each account
 *  7) (Optionally) Prints "Split payment of {amount} {currency}"
 */
public class SplitPaymentCommand implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;
    private final ExchangeRateManager exchangeRateManager;

    public SplitPaymentCommand(ObjectMapper objectMapper,
                               ArrayNode output,
                               ExchangeRateManager exchangeRateManager) {
        this.objectMapper = objectMapper;
        this.output = output;
        this.exchangeRateManager = exchangeRateManager;
    }

    @Override
    public void execute(List<User> users, CommandInput command) {
        // 1) Find the accounts
        List<String> ibans = command.getAccounts();
        if (ibans == null || ibans.isEmpty()) {
            generateOutput("No accounts provided for split payment.", command.getTimestamp());
            return;
        }

        List<Account> involvedAccounts = findAccounts(ibans, users);
        if (involvedAccounts.size() != ibans.size()) {
            generateOutput("User not found", command.getTimestamp());
            return;
        }

        // 2) Select the strategy
        SplitPaymentStrategy strategy = selectStrategy(command.getSplitPaymentType());

        // 3) Build the map of (Account -> amount)
        Map<Account, Double> splitMap;
        try {
            splitMap = strategy.calculateSplit(involvedAccounts, command, exchangeRateManager);
        } catch (Exception e) {
            generateOutput(e.getMessage(), command.getTimestamp());
            return;
        }

        // 4) Create a pending operation object
        //    If "custom", use SplitCustomPaymentOperation; otherwise, use the old SplitPaymentOperation
        Operation pendingOp;
        if ("custom".equalsIgnoreCase(command.getSplitPaymentType())) {
            // Create a specialized custom operation
            // (Adjust the constructor parameters to match your SplitCustomPaymentOperation)
            pendingOp = new SplitCustomPaymentOperation(
                    command.getTimestamp(),
                    command.getAmount(),
                    command.getCurrency(),
                    "Split payment of " + String.format("%.2f", command.getAmount()) + " " + command.getCurrency(),
                    command.getAccounts(),
                    command.getAmountForUsers(),
                    "custom"
            );
        } else {
            // Use the original SplitPaymentOperation for "equal" (or default)
            pendingOp = new SplitPaymentOperation(
                    command.getTimestamp(),
                    command.getAmount(),
                    command.getCurrency(),
                    "Split payment of " + String.format("%.2f", command.getAmount()) + " " + command.getCurrency(),
                    null,
                    command.getSplitPaymentType(),
                    splitMap
            );
        }

        // 5) Create a SplitPaymentState and add it to the manager
        SplitPaymentState state = new SplitPaymentState(
                command.getSplitPaymentType(),
                new HashSet<>(involvedAccounts),
                splitMap,
                pendingOp,
                command.getTimestamp()
        );

        SplitPaymentManager manager = SplitPaymentManager.getInstance();
        manager.addSplit(state);

        // 6) Mark this operation as pending for each account
        for (Account acc : involvedAccounts) {
            acc.addPendingOperation(pendingOp);
        }

        // (Note: The original code does not print a success message at the end,
        // so we leave it out unless you want to add it.)
    }

    private SplitPaymentStrategy selectStrategy(String splitPaymentType) {
        if ("custom".equalsIgnoreCase(splitPaymentType)) {
            return new CustomSplitPaymentStrategy();
        } else {
            // Default to "equal"
            return new EqualSplitPaymentStrategy();
        }
    }

    private List<Account> findAccounts(List<String> ibans, List<User> users) {
        return ibans.stream()
                .flatMap(iban -> users.stream()
                        .flatMap(u -> u.getAccounts().stream())
                        .filter(acc -> acc.getIban().equals(iban)))
                .collect(Collectors.toList());
    }

    private void generateOutput(String message, int timestamp) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("timestamp", timestamp);
        node.put("description", message);
        output.add(node);
    }
}
