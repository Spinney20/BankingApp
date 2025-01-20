package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.Operation;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.SplitCustomPaymentOperation;
import org.poo.operationTypes.SplitEqualPaymentOperation;
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
 *  4) Creates the appropriate Operation (SplitCustomPaymentOperation or SplitEqualPaymentOperation)
 *  5) Creates a SplitPaymentState and adds it to the manager
 *  6) Adds the pending operation to each account
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
    public void execute(List<User> users, final List<Commerciant> commerciants, CommandInput command) {
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

        // 2) Determine the strategy
        SplitPaymentStrategy strategy = selectStrategy(command.getSplitPaymentType());

        // 3) Calculate the split map
        Map<Account, Double> splitMap;
        try {
            splitMap = strategy.calculateSplit(involvedAccounts, command, exchangeRateManager);
        } catch (Exception e) {
            generateOutput(e.getMessage(), command.getTimestamp());
            return;
        }

        // 4) Build the Operation object
        Operation pendingOp;
        String splitType = command.getSplitPaymentType();
        if ("custom".equalsIgnoreCase(splitType)) {
            // --- CUSTOM SPLIT ---
            pendingOp = new SplitCustomPaymentOperation(
                    command.getTimestamp(),
                    command.getAmount(),
                    command.getCurrency(),
                    "Split payment of " + String.format("%.2f", command.getAmount()) + " " + command.getCurrency(),
                    command.getAccounts(),         // List<String> of IBANs
                    command.getAmountForUsers(),   // List<Double> user amounts
                    "custom"
            );
        } else {
            // --- EQUAL SPLIT ---
            // Build an ArrayNode of IBANs so we can pass it to SplitEqualPaymentOperation
            ArrayNode arrayNodeIbans = objectMapper.createArrayNode();
            for (String iban : ibans) {
                arrayNodeIbans.add(iban);
            }

            pendingOp = new SplitEqualPaymentOperation(
                    command.getTimestamp(),
                    command.getAmount(),
                    command.getCurrency(),
                    "Split payment of " + String.format("%.2f", command.getAmount()) + " " + command.getCurrency(),
                    arrayNodeIbans,
                    // If 'splitPaymentType' is null or not "equal", we can default to "equal"
                    (splitType == null ? "equal" : splitType),
                    splitMap
            );
        }

        // 5) Create a SplitPaymentState and add it to the manager
        SplitPaymentState state = new SplitPaymentState(
                splitType,
                new HashSet<>(involvedAccounts),
                splitMap,
                pendingOp,
                command.getTimestamp()
        );

        SplitPaymentManager manager = SplitPaymentManager.getInstance();
        manager.addSplit(state);

        // 6) Mark pending on each involved account
        for (Account acc : involvedAccounts) {
            acc.addPendingOperation(pendingOp);
        }
    }

    private SplitPaymentStrategy selectStrategy(String splitPaymentType) {
        if ("custom".equalsIgnoreCase(splitPaymentType)) {
            return new CustomSplitPaymentStrategy();
        }
        return new EqualSplitPaymentStrategy();
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
