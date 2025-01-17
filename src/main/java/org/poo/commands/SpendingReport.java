package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Operation;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.CardPaymentOperation;

import java.util.*;

public class SpendingReport implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public SpendingReport(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Just like a report but with only payments
     * and different layout
     * finding the acc etc, also this is possible on
     * only regular accs and I have an error if it is applied
     * to a savings acc
     * I also do a sum for each commerciant
     * and then sorting commerciants by those sums, thats it
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final CommandInput command) {
        Account targetAccount = null;

        // Finding the acc
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(command.getAccount())) {
                    targetAccount = account;
                    break;
                }
            }
            if (targetAccount != null) {
                break;
            }
        }

        // Acc nonexistent err
        if (targetAccount == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("command", "spendingsReport");

            ObjectNode outputNode = objectMapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", command.getTimestamp());

            errorResponse.set("output", outputNode);
            errorResponse.put("timestamp", command.getTimestamp());

            output.add(errorResponse);
            return;
        }

        if (targetAccount.getAccountType().equals("savings")) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("command", "spendingsReport");

            ObjectNode outputDetails = objectMapper.createObjectNode();
            outputDetails.put("error", "This kind of report is not supported for a saving account");

            errorResponse.set("output", outputDetails);
            errorResponse.put("timestamp", command.getTimestamp());
            output.add(errorResponse);
            return;
        }

        ArrayNode transactionsOutput = objectMapper.createArrayNode();
        Map<String, Double> commerciantTotals = new HashMap<>();

        // Iterating to the operations of the acc
        for (Operation operation : targetAccount.getOperations()) {
            if (operation.getTimestamp() >= command.getStartTimestamp()
                    && operation.getTimestamp() <= command.getEndTimestamp()
                    && operation.getOperationType().equals("cardPayment")) {

                CardPaymentOperation cardPayment = (CardPaymentOperation) operation;

                // Adding the operation (transaction) to the transactions
                ObjectNode transactionNode = objectMapper.createObjectNode();
                transactionNode.put("timestamp", cardPayment.getTimestamp());
                transactionNode.put("description", cardPayment.getDescription());
                transactionNode.put("amount", cardPayment.getAmount());
                transactionNode.put("commerciant", cardPayment.getCommerciant());
                transactionsOutput.add(transactionNode);

                // Summing up total per commerciant
                commerciantTotals.put(cardPayment.getCommerciant(),
                        commerciantTotals.getOrDefault(cardPayment.getCommerciant(), 0.0)
                                + cardPayment.getAmount());
            }
        }

        // Sorting the commerciants
        List<Map.Entry<String, Double>> sortedCommerciants =
                new ArrayList<>(commerciantTotals.entrySet());
        sortedCommerciants.sort(Map.Entry.comparingByKey());

        // The list of commerciants
        ArrayNode commerciantsOutput = objectMapper.createArrayNode();
        for (Map.Entry<String, Double> entry : sortedCommerciants) {
            ObjectNode commerciantNode = objectMapper.createObjectNode();
            commerciantNode.put("commerciant", entry.getKey());
            commerciantNode.put("total", entry.getValue());
            commerciantsOutput.add(commerciantNode);
        }

        // the output
        ObjectNode spendingsReportOutput = objectMapper.createObjectNode();
        spendingsReportOutput.put("IBAN", targetAccount.getIban());
        spendingsReportOutput.put("balance", targetAccount.getBalance());
        spendingsReportOutput.put("currency", targetAccount.getCurrency());
        spendingsReportOutput.set("transactions", transactionsOutput);
        spendingsReportOutput.set("commerciants", commerciantsOutput);

        ObjectNode finalOutput = objectMapper.createObjectNode();
        finalOutput.put("command", "spendingsReport");
        finalOutput.set("output", spendingsReportOutput);
        finalOutput.put("timestamp", command.getTimestamp());

        output.add(finalOutput);
    }
}
