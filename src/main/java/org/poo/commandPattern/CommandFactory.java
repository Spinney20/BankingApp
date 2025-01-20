package org.poo.commandPattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.commands.*;
import org.poo.currencyExchange.ExchangeRateManager;

public class CommandFactory {
    private final ExchangeRateManager exchangeRateManager;
    private final ObjectMapper objectMapper;
    private final ArrayNode output;

    public CommandFactory(final ExchangeRateManager exchangeRateManager,
                          final ObjectMapper objectMapper, final ArrayNode output) {
        this.exchangeRateManager = exchangeRateManager;
        this.objectMapper = objectMapper;
        this.output = output;
    }

    /***
     * Creates a command based on the command given
     * Each command possible has a different class
     * If the command is not recognized, an exception is thrown
     * it is never use tho, but its just for the plot xD
     * @param commandType - the type of the command
     * @return each command
     */
    public Command createCommand(final String commandType) {
        return switch (commandType) {
            case "printUsers" -> new PrintUsers(objectMapper, output);
            case "addAccount" -> new AddAccountCommand(exchangeRateManager);
            case "createCard" -> new CreateCardCommand();
            case "addFunds" -> new AddFundsCommand();
            case "deleteAccount" -> new DeleteAccountCommand(objectMapper, output);
            case "createOneTimeCard" -> new CreateOneTimeCommand();
            case "deleteCard" -> new DeleteCardCommand();
            case "payOnline" -> new PayOnlineCommand(objectMapper, output, exchangeRateManager);
            case "sendMoney" -> new SendMoneyCommand(exchangeRateManager, objectMapper, output);
            case "setAlias" -> new SetAliasCommand();
            case "printTransactions" -> new PrintTransactions(objectMapper, output);
            case "setMinimumBalance" -> new SetMinimumBalance(objectMapper, output);
            case "checkCardStatus" -> new CheckCardStatus(objectMapper, output);
            case "splitPayment" -> new SplitPaymentCommand(objectMapper , output, exchangeRateManager);
            case "report" -> new Report(objectMapper, output);
            case "spendingsReport" -> new SpendingReport(objectMapper, output);
            case "changeInterestRate" -> new ChangeInterestCommand(objectMapper, output);
            case "addInterest" -> new AddInterestCommand(objectMapper, output);
            case "withdrawSavings" -> new WithdrawSavingsCommand(objectMapper, output,
                    exchangeRateManager);
            case "upgradePlan" -> new UpgradePlanCommand(exchangeRateManager, objectMapper, output);
            case "cashWithdrawal" -> new CashWithdrawalCommand(exchangeRateManager, objectMapper, output);
            case "acceptSplitPayment" -> new AcceptSplitPaymentCommand(objectMapper, output);
            case "rejectSplitPayment" -> new RejectSplitPaymentCommand(objectMapper, output);
            case "addNewBusinessAssociate" -> new AddNewBusinessAssociateCommand();
            case "changeSpendingLimit" -> new ChangeSpendingLimitCommand(objectMapper, output);
            case "businessReport" -> new BusinessReportCommand(objectMapper, output);
            case "changeDepositLimit" -> new ChangeDepositLimitCommand(objectMapper, output);
            default -> {
                System.out.println("Unknown command type: " + commandType);
                yield null; // Return null for unrecognized commands
            }
        };
    }
}
