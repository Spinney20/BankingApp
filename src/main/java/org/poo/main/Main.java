package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.commandPattern.CommandFactory;
import org.poo.commandPattern.CommandInvoker;
import org.poo.currencyExchange.ExchangeRate;
import org.poo.currencyExchange.ExchangeRateManager;
import org.poo.data.*;
import org.poo.fileio.CommandInput;
import org.poo.fileio.CommerciantInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        var sortedFiles = Arrays.stream(Objects.requireNonNull(directory.listFiles())).
                sorted(Comparator.comparingInt(Main::fileConsumer))
                .toList();

        for (File file : sortedFiles) {
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(CheckerConstants.TESTS_PATH + filePath1);
        ObjectInput inputData = objectMapper.readValue(file, ObjectInput.class);
        Utils.resetRandom();

        // Conversion UserInput -> User
        List<User> users = new ArrayList<>();
        for (UserInput userInput : inputData.getUsers()) {
            User user = new User(
                    userInput.getFirstName(),
                    userInput.getLastName(),
                    userInput.getEmail(),
                    LocalDate.parse(userInput.getBirthDate()), // Assuming birthDate is in ISO format (e.g., "2000-12-24")
                    userInput.getOccupation()
            );
            users.add(user);
        }

        // Conversion CommerciantInput -> Commerciant
        List<Commerciant> commerciants = new ArrayList<>();
        for (CommerciantInput commerciantInput : inputData.getCommerciants()) {
            Commerciant commerciant = new Commerciant(
                    commerciantInput.getCommerciant(),
                    commerciantInput.getType(),
                    commerciantInput.getCashbackStrategy(),
                    commerciantInput.getAccount()
            );
            commerciants.add(commerciant);
        }

        // Setting up the exchange rate Manager
        ArrayNode output = objectMapper.createArrayNode();
        List<ExchangeRate> exchangeRates = Arrays.stream(inputData.getExchangeRates())
                .map(input -> new ExchangeRate(input.getFrom(), input.getTo(), input.getRate()))
                .toList();

        ExchangeRateManager exchangeRateManager = new ExchangeRateManager(exchangeRates);

        // Factory + Invoker for command management (command pattern)
        // the factory is used to create the commands
        // the invoker is used to execute the commands
        CommandFactory commandFactory = new CommandFactory(exchangeRateManager,
                objectMapper, output);
        CommandInvoker invoker = new CommandInvoker(commandFactory);

        // Here is where the magic happens - executing the commands
        for (CommandInput command : inputData.getCommands()) {
            invoker.executeCommand(command.getCommand(), users, commerciants, command);
        }

        // Writing the output to the file
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }


    /**
     * Method used for extracting the test number from the file name.
     *
     * @param file the input file
     * @return the extracted numbers
     */
    public static int fileConsumer(final File file) {
        return Integer.parseInt(
                file.getName()
                        .replaceAll(CheckerConstants.DIGIT_REGEX, CheckerConstants.EMPTY_STR)
        );
    }
}
