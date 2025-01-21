package org.poo.commands;

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

import java.util.List;

public class Report implements Command {
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public Report(final ObjectMapper objectMapper, final ArrayNode output) {
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Report command handling
     * It prints the transactions between two timestamps
     * For me its just a print transactions command but
     * between two timestamps, pretty easy
     * I know i could have added smth to not have the same code
     * for the switch cases but idk
     * @param users - list of users
     * @param command - the command to be executed
     */
    @Override
    public void execute(final List<User> users, final List<Commerciant> commerciants,
                        final CommandInput command) {
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

        // Error acc not found
        if (targetAccount == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("command", "report");

            ObjectNode outputNode = objectMapper.createObjectNode();
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", command.getTimestamp());

            errorResponse.set("output", outputNode);
            errorResponse.put("timestamp", command.getTimestamp());

            output.add(errorResponse);
            return;
        }

        ArrayNode transactionsOutput = objectMapper.createArrayNode();

        // Just like on print transaction but beetween some timestamps
        for (Operation operation : targetAccount.getOperations()) {
            if (operation.getTimestamp() >= command.getStartTimestamp()
                    && operation.getTimestamp() <= command.getEndTimestamp()) {

                ObjectNode operationNode = objectMapper.createObjectNode();
                operationNode.put("timestamp", operation.getTimestamp());

                switch (operation.getOperationType()) {
                    case "transaction":
                        TransactionOperation transaction = (TransactionOperation) operation;
                        operationNode.put("description", transaction.getDescription());
                        operationNode.put("senderIBAN", transaction.getSenderIBAN());
                        operationNode.put("receiverIBAN", transaction.getReceiverIBAN());
                        operationNode.put("amount",
                                transaction.getAmount() + " " + transaction.getCurrency());
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
                        operationNode.put("description", checkCardStatusOperation.getDescription());
                        break;

                    case "SplitPayment":
                        SplitEqualPaymentOperation splitEqualPaymentOperation
                                = (SplitEqualPaymentOperation) operation;
                        operationNode.set("amount",
                                objectMapper.getNodeFactory().
                                        numberNode(splitEqualPaymentOperation.getAmount()));
                        operationNode.set("currency",
                                objectMapper.getNodeFactory().
                                        textNode(splitEqualPaymentOperation.getCurrency()));
                        operationNode.set("description",
                                objectMapper.getNodeFactory().
                                        textNode(splitEqualPaymentOperation.getDescription()));
                        operationNode.set("involvedAccounts",
                                splitEqualPaymentOperation.getInvolvedAccounts());
                        operationNode.set("splitPaymentType",
                                objectMapper.getNodeFactory().
                                        textNode(splitEqualPaymentOperation.getSplitPaymentType()));
                        break;


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
                    case "upgradePlan":
                        UpgradePlanOperation upgradePlanOperation
                                = (UpgradePlanOperation) operation;
                        operationNode.put("accountIBAN", upgradePlanOperation.getAccountIBAN());
                        operationNode.put("newPlanType", upgradePlanOperation.getNewPlanType());
                        operationNode.put("description", "Upgrade plan");
                        break;
                    case "addInterest":
                        AddInterestOperation addInterestOperation
                                = (AddInterestOperation) operation;
                        operationNode.put("amount", addInterestOperation.getAmount());
                        operationNode.put("currency", addInterestOperation.getCurrency());
                        operationNode.put("description", addInterestOperation.getDescription());
                        break;
                    case "info":
                        InfoOperation infoOperation = (InfoOperation) operation;
                        operationNode.put("description", infoOperation.getDescription());
                        break;
                    default:
                        operationNode.put("description", "Unknown operation type");
                }

                transactionsOutput.add(operationNode);
            }
        }

        // The output
        ObjectNode reportOutput = objectMapper.createObjectNode();
        reportOutput.put("command", "report");

        ObjectNode outputNode = objectMapper.createObjectNode();
        outputNode.put("balance", targetAccount.getBalance());
        outputNode.put("currency", targetAccount.getCurrency());
        outputNode.put("IBAN", targetAccount.getIban());
        outputNode.set("transactions", transactionsOutput);

        reportOutput.set("output", outputNode);
        reportOutput.put("timestamp", command.getTimestamp());

        output.add(reportOutput);
    }
}
