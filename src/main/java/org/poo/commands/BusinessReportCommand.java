package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.accountTypes.BusinessAccount;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.Stats;
import org.poo.data.User;
import org.poo.fileio.CommandInput;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class BusinessReportCommand implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public BusinessReportCommand(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Generates a business report for a business account.
     * The report can be of two types:
     * - "transaction" - shows the total amount spent and deposited by each associate
     *  and the total amount spent and deposited by all associates
     *  - "commerciant" - shows the total amount received from each commerciant
     * @param users - list of users
     * @param commerciants
     * @param cmd - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants,
                        final CommandInput cmd) {
        String accountIban = cmd.getAccount();
        String reportType = cmd.getType(); // "transaction" or "commerciant"
        int startTimestamp = cmd.getStartTimestamp();
        int endTimestamp = cmd.getEndTimestamp();

        Account account = findAccountByIban(accountIban, users);

        if (account == null) {
            addErrorNode("businessReport", cmd.getTimestamp(), "Account not found");
            return;
        }

        BusinessAccount businessAccount = (BusinessAccount) account;

        if ("transaction".equals(reportType)) {
            generateTransactionReport(cmd, businessAccount, users, startTimestamp, endTimestamp);
        } else if ("commerciant".equals(reportType)) {
            generateCommerciantReport(cmd, businessAccount, users, startTimestamp, endTimestamp);
        } else {
            addErrorNode("businessReport", cmd.getTimestamp(), "Invalid report type");
        }
    }

    private void generateTransactionReport(final CommandInput cmd,
                                           final BusinessAccount businessAccount,
                                           final List<User> users,
                                           final int startTimestamp, final int endTimestamp) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("command", "businessReport");
        node.put("timestamp", cmd.getTimestamp());

        ObjectNode outputNode = objectMapper.createObjectNode();
        outputNode.put("IBAN", businessAccount.getIban());
        outputNode.put("balance", businessAccount.getBalance());
        outputNode.put("currency", businessAccount.getCurrency());
        outputNode.put("spending limit", businessAccount.getGlobalSpendingLimit());
        outputNode.put("deposit limit", businessAccount.getGlobalDepositLimit());
        outputNode.put("statistics type", "transaction");

        ArrayNode managersArray = objectMapper.createArrayNode();
        ArrayNode employeesArray = objectMapper.createArrayNode();

        double totalSpent = 0.0;
        double totalDeposited = 0.0;

        for (Map.Entry<String, String> associateEntry
                : businessAccount.getAssociates().entrySet()) {
            String associateEmail = associateEntry.getKey();
            String role = associateEntry.getValue();
            User associate = findUserByEmail(users, associateEmail);

            if (associate != null) {
                Stats stats = businessAccount.getStatsMap().
                        getOrDefault(associateEmail, new Stats());
                double spent = stats.getSpent();
                double deposited = stats.getDeposited();

                totalSpent += spent;
                totalDeposited += deposited;

                ObjectNode userNode = objectMapper.createObjectNode();
                userNode.put("username", associate.getLastName() + " " + associate.getFirstName());
                userNode.put("spent", spent);
                userNode.put("deposited", deposited);

                if ("manager".equals(role)) {
                    managersArray.add(userNode);
                } else if ("employee".equals(role)) {
                    employeesArray.add(userNode);
                }
            }
        }

        outputNode.set("managers", managersArray);
        outputNode.set("employees", employeesArray);
        outputNode.put("total spent", totalSpent);
        outputNode.put("total deposited", totalDeposited);

        node.set("output", outputNode);
        output.add(node);
    }

    private void generateCommerciantReport(final CommandInput cmd,
                                           final BusinessAccount businessAccount,
                                           final List<User> users,
                                           final int startTimestamp,
                                           final int endTimestamp) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("command", "businessReport");
        node.put("timestamp", cmd.getTimestamp());

        ObjectNode outputNode = objectMapper.createObjectNode();
        ArrayNode commerciantsArray = objectMapper.createArrayNode();

        outputNode.put("IBAN", businessAccount.getIban());
        outputNode.put("balance", businessAccount.getBalance());
        outputNode.put("currency", businessAccount.getCurrency());
        outputNode.put("spending limit", businessAccount.getGlobalSpendingLimit());
        outputNode.put("deposit limit", businessAccount.getGlobalDepositLimit());
        outputNode.put("statistics type", "commerciant");

        Map<String, Double> commerciantTransactions = businessAccount.getCommerciants();

        Map<String, Map<String, Double>> userSpentOnCommerciant
                = businessAccount.getUserSpentOnCommerciant();
        Map<String, Map<String, Integer>> userTxCountOnCommerciant
                = businessAccount.getUserTxCountOnCommerciant();

        for (Map.Entry<String, Double> entry : commerciantTransactions.entrySet()) {
            String commerciantName = entry.getKey();
            double totalReceived = entry.getValue();

            ObjectNode commerciantNode = objectMapper.createObjectNode();
            commerciantNode.put("commerciant", commerciantName);
            commerciantNode.put("total received", totalReceived);

            ArrayNode managersArray = objectMapper.createArrayNode();
            ArrayNode employeesArray = objectMapper.createArrayNode();

            // B) For the sums
            Map<String, Double> userMap
                    = userSpentOnCommerciant.getOrDefault(commerciantName, new HashMap<>());

            // C) For the transaction counts
            Map<String, Integer> txCountMap
                    = userTxCountOnCommerciant.getOrDefault(commerciantName, new HashMap<>());

            // For each user who spent something
            for (Map.Entry<String, Double> userSpentEntry : userMap.entrySet()) {
                String email = userSpentEntry.getKey();
                double spentForThisCommerciant = userSpentEntry.getValue();
                if (spentForThisCommerciant <= 0) {
                    continue;
                }

                // find role
                String role = businessAccount.getAssociates().get(email);
                if (role == null) {
                    continue;
                }

                // find user
                User user = findUserByEmail(users, email);
                if (user == null) {
                    continue;
                }

                String fullName = user.getLastName() + " " + user.getFirstName();

                // transaction count => how many times to insert them
                int txCount = txCountMap.getOrDefault(email, 0);

                // Add them 'txCount' times
                if ("manager".equalsIgnoreCase(role)) {
                    for (int i = 0; i < txCount; i++) {
                        managersArray.add(fullName);
                    }
                } else if ("employee".equalsIgnoreCase(role)) {
                    for (int i = 0; i < txCount; i++) {
                        employeesArray.add(fullName);
                    }
                }
            }

            commerciantNode.set("managers", managersArray);
            commerciantNode.set("employees", employeesArray);

            commerciantsArray.add(commerciantNode);
        }

        // optional sorting
        sortArrayNodeByField(commerciantsArray, "commerciant");

        outputNode.set("commerciants", commerciantsArray);
        node.set("output", outputNode);
        output.add(node);
    }

    private User findUserByEmail(final List<User> users, final String email) {
        return users.stream().
                filter(user -> user.getEmail().equals(email)).findFirst().orElse(null);
    }

    private Account findAccountByIban(final String iban, final List<User> users) {
        return users.stream()
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getIban().equals(iban))
                .findFirst()
                .orElse(null);
    }

    private void addErrorNode(final String command, final int timestamp,
                              final String errorMessage) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("command", command);
        node.put("timestamp", timestamp);
        node.put("error", errorMessage);
        output.add(node);
    }

    private void sortArrayNodeByField(final ArrayNode arrayNode, final String fieldName) {
        List<ObjectNode> sortedList = StreamSupport.stream(arrayNode.spliterator(), false)
                .map(node -> (ObjectNode) node)
                .sorted(Comparator.comparing(o -> o.get(fieldName).asText()))
                .toList();

        arrayNode.removeAll();
        sortedList.forEach(arrayNode::add);
    }
}
