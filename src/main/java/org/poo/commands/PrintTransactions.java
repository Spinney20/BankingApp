package org.poo.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.commandPattern.Command;
import org.poo.data.Account;
import org.poo.data.Commerciant;
import org.poo.data.Operation;
import org.poo.data.User;
import org.poo.fileio.CommandInput;
import org.poo.operationTypes.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PrintTransactions implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public PrintTransactions(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Handles the printTransactions command which
     * prints the operations held in each account
     * First, I find the user with the given email
     * Then, I sort all operations by timestamp
     * I iterate through the sorted operations and add them to the output
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants, final CommandInput command) {
        User userWithTransactions = null;

        // Find the user
        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                userWithTransactions = user;
                break;
            }
        }

        if (userWithTransactions != null) {
            ObjectNode transactionsResponse = objectMapper.createObjectNode();
            transactionsResponse.put("command", "printTransactions");
            ArrayNode transactionsOutput = objectMapper.createArrayNode();

            // Putting all operations in a temp list so I can sort them
            List<Operation> allOperations = new ArrayList<>();
            for (Account account : userWithTransactions.getAccounts()) {
                allOperations.addAll(account.getOperations());
            }

            // Sort all operations by timestamp
            allOperations.sort(Comparator.comparingInt(Operation::getTimestamp));

            // Iterate through the sorted operations
            for (Operation operation : allOperations) {
                ObjectNode operationNode = objectMapper.createObjectNode();
                operationNode.put("timestamp", operation.getTimestamp());

                switch (operation.getOperationType()) {
                    case "transaction":
                        TransactionOperation transaction = (TransactionOperation) operation;
                        operationNode.put("description", transaction.getDescription());
                        operationNode.put("senderIBAN", transaction.getSenderIBAN());
                        operationNode.put("receiverIBAN", transaction.getReceiverIBAN());
                        operationNode.put("amount", transaction.getAmount()
                                + " " + transaction.getCurrency());
                        operationNode.put("transferType", transaction.getTransferType());
                        break;

                    case "accountCreation":
                        AccountCreationOperation accountCreation =
                                (AccountCreationOperation) operation;
                        operationNode.put("description", accountCreation.getDescription());
                        break;

                    case "cardCreation":
                        CreateCardOperation createCard = (CreateCardOperation) operation;
                        operationNode.put("account", createCard.getAccountIBAN());
                        operationNode.put("card", createCard.getCardNumber());
                        operationNode.put("cardHolder", createCard.getCardHolder());
                        operationNode.put("description", createCard.getDescription());
                        break;

                    case "cardPayment":
                        CardPaymentOperation cardPayment = (CardPaymentOperation) operation;
                        operationNode.put("amount", cardPayment.getAmount());
                        operationNode.put("commerciant", cardPayment.getCommerciant());
                        operationNode.put("description", cardPayment.getDescription());
                        break;

                    case "failure":
                        FailOperation failOperation = (FailOperation) operation;
                        operationNode.put("description", failOperation.getDescription());
                        break;

                    case "deleteCard":
                        DeleteCardOperation deleteCardOperation = (DeleteCardOperation) operation;
                        operationNode.put("account", deleteCardOperation.getAccount());
                        operationNode.put("card", deleteCardOperation.getCardNumber());
                        operationNode.put("cardHolder", deleteCardOperation.getCardHolder());
                        operationNode.put("description", deleteCardOperation.getDescription());
                        break;

                    case "CheckCardStatus":
                        CheckCardStatusOperation checkCardStatusOperation =
                                (CheckCardStatusOperation) operation;
                        operationNode.put("description",
                                checkCardStatusOperation.getDescription());
                        break;

                    case "SplitPaymentEQUAL": {
                        SplitEqualPaymentOperation splitEqualOp = (SplitEqualPaymentOperation) operation;

                        // 3) "amount" => show the share rather than the total
                        operationNode.set("amount",
                                objectMapper.getNodeFactory().numberNode(splitEqualOp.getAmount() /
                                        splitEqualOp.getSplitMap().size()));

                        // 4) "currency"
                        operationNode.set("currency",
                                objectMapper.getNodeFactory().textNode(splitEqualOp.getCurrency()));

                        // 5) "description"
                        String desc = String.format("Split payment of %.2f %s",
                                splitEqualOp.getAmount(),
                                splitEqualOp.getCurrency());
                        operationNode.set("description",
                                objectMapper.getNodeFactory().textNode(desc));

                        // 6) If there’s an error (insufficient funds), display it
                        if (splitEqualOp.getError() != null && !splitEqualOp.getError().isEmpty()) {
                            operationNode.set("error",
                                    objectMapper.getNodeFactory().textNode(splitEqualOp.getError()));
                        }

                        // 7) "involvedAccounts" => attach the existing ArrayNode
                        // Convert the existing ArrayNode of IBANs to a new array
                        ArrayNode originalAccounts = splitEqualOp.getInvolvedAccounts();
                        ArrayNode newAccountsArray = objectMapper.createArrayNode();

                        for (JsonNode ibanNode : originalAccounts) {
                            // .asText() converts the JsonNode to a string
                            String iban = ibanNode.asText();
                            newAccountsArray.add(iban);
                        }

                        operationNode.set("involvedAccounts", newAccountsArray);;

                        // 8) "splitPaymentType"
                        operationNode.set("splitPaymentType",
                                objectMapper.getNodeFactory().textNode(splitEqualOp.getSplitPaymentType()));

                        // 9) "timestamp"
                        operationNode.set("timestamp",
                                objectMapper.getNodeFactory().numberNode(splitEqualOp.getTimestamp()));

                        break;
                    }



                    case "SplitPaymentFail":
                        SplitPaymentFailOperation splitPaymentFailOperation =
                                (SplitPaymentFailOperation) operation;
                        operationNode.set("amount",
                                objectMapper.getNodeFactory().
                                        numberNode(splitPaymentFailOperation.getAmount()));
                        operationNode.set("currency",
                                objectMapper.getNodeFactory().
                                        textNode(splitPaymentFailOperation.getCurrency()));
                        operationNode.set("description",
                                objectMapper.getNodeFactory().
                                        textNode(splitPaymentFailOperation.getDescription()));
                        operationNode.set("error",
                                objectMapper.getNodeFactory().
                                        textNode(splitPaymentFailOperation.getError()));
                        operationNode.set("involvedAccounts",
                                splitPaymentFailOperation.getInvolvedAccounts());
                        // Add splitPaymentType to the output
                        operationNode.set("splitPaymentType",
                                objectMapper.getNodeFactory().
                                        textNode(splitPaymentFailOperation.getSplitPaymentType()));
                        break;

                    case "SplitPaymentCUSTOM": {
                        SplitCustomPaymentOperation splitCustomPaymentOperation =
                                (SplitCustomPaymentOperation) operation;

                        // "amountForUsers"
                        ArrayNode amountsArray = objectMapper.createArrayNode();
                        for (Double userAmount : splitCustomPaymentOperation.getAmountForUsers()) {
                            amountsArray.add(userAmount);
                        }
                        operationNode.set("amountForUsers", amountsArray);

                        // "currency"
                        operationNode.set("currency",
                                objectMapper.getNodeFactory().textNode(splitCustomPaymentOperation.getCurrency()));

                        // "description"
                        operationNode.set("description",
                                objectMapper.getNodeFactory().textNode(splitCustomPaymentOperation.getDescription()));

                        // If there's an error, show it
                        if (splitCustomPaymentOperation.getError() != null) {
                            operationNode.set("error",
                                    objectMapper.getNodeFactory().textNode(splitCustomPaymentOperation.getError()));
                        }

                        // "involvedAccounts"
                        ArrayNode accountsArray = objectMapper.createArrayNode();
                        for (String iban : splitCustomPaymentOperation.getInvolvedAccounts()) {
                            accountsArray.add(iban);
                        }
                        operationNode.set("involvedAccounts", accountsArray);

                        // "splitPaymentType"
                        operationNode.set("splitPaymentType",
                                objectMapper.getNodeFactory().textNode(splitCustomPaymentOperation.getSplitPaymentType()));

                        // "timestamp"
                        operationNode.set("timestamp",
                                objectMapper.getNodeFactory().numberNode(splitCustomPaymentOperation.getTimestamp()));

                        break;
                    }
                    case "info":
                        InfoOperation infoOperation = (InfoOperation) operation;
                        operationNode.put("description", infoOperation.getDescription());
                        break;
                    case "upgradePlan":
                        UpgradePlanOperation upgradePlanOperation = (UpgradePlanOperation) operation;
                        operationNode.put("accountIBAN", upgradePlanOperation.getAccountIBAN());
                        operationNode.put("newPlanType", upgradePlanOperation.getNewPlanType());
                        operationNode.put("description", "Upgrade plan");
                        break;
                    case "cashWithdrawal":
                        CashWithdrawalOperation cashWithdrawalOperation =
                                (CashWithdrawalOperation) operation;
                        operationNode.put("amount", cashWithdrawalOperation.getAmount());
                        operationNode.put("description", cashWithdrawalOperation.getDescription());
                        break;
                    case "withdrawSavingsFail":
                        WithdrawSavingsFailOperation withdrawSavingsFailOperation =
                                (WithdrawSavingsFailOperation) operation;
                        operationNode.put("description", withdrawSavingsFailOperation.getDescription());
                        break;
                    case "addInterest":
                        AddInterestOperation addInterestOperation = (AddInterestOperation) operation;
                        operationNode.put("amount", addInterestOperation.getAmount());
                        operationNode.put("currency", addInterestOperation.getCurrency());
                        operationNode.put("description", addInterestOperation.getDescription());
                        break;
                    case "withdrawSav":
                        WithdrawSavingsOperation transaction2 = (WithdrawSavingsOperation) operation;
                        operationNode.put("classicAccountIBAN", transaction2.getReceiverIBAN());
                        operationNode.put("description", transaction2.getDescription());
                        operationNode.put("savingsAccountIBAN", transaction2.getSenderIBAN());
                        operationNode.put("amount", transaction2.getAmount());
                        break;
                    default:
                        break;
                }

                // Add the operation to the JSON output
                transactionsOutput.add(operationNode);
            }

            // Build the final response
            transactionsResponse.set("output", transactionsOutput);
            transactionsResponse.put("timestamp", command.getTimestamp());

            // Add the response to the main output
            output.add(transactionsResponse);
        }
    }
}
