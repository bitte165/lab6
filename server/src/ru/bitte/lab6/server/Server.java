package ru.bitte.lab6.server;

import org.xml.sax.SAXException;
import ru.bitte.lab6.AbstractCommandRequest;
import ru.bitte.lab6.ArgumentCommandRequest;
import ru.bitte.lab6.commands.*;
import ru.bitte.lab6.exceptions.ClientDisconnectedException;
import ru.bitte.lab6.exceptions.ElementException;
import ru.bitte.lab6.exceptions.ElementParsingInFileException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Server {
//    private final Scanner in;
    @SuppressWarnings("FieldCanBeLocal")
    private final CollectionKeeper collection;
    private final Parser parser = new Parser();
    private final Map<String, Command> commands;
    private final Deque<String> history;
    private ServerConnector serverConnector;
    private final Logger logger;

    public Server(String fileName)
            throws TransformerConfigurationException, ParserConfigurationException, ElementParsingInFileException,
            IOException, SAXException {
//        in = new Scanner(System.in);
        // initialize the collection keeper with a file
        collection = new CollectionKeeper(parser.readFromFile(new File(fileName)));
        // initialize the commands by first getting them in a hashset and then adding to a hashmap in a loop
        history = new ArrayDeque<>(15);
        commands = new HashMap<>();
        Set<Command> tempComs = new HashSet<>();
        tempComs.add(new AddCommand(collection));
        tempComs.add(new AddIfMinCommand(collection));
        tempComs.add(new ClearCommand(collection));
        tempComs.add(new FilterCommand(collection));
        tempComs.add(new HelpCommand(commands));
        tempComs.add(new HistoryCommand(history));
        tempComs.add(new InfoCommand(collection));
        tempComs.add(new PrintAscendingCommand(collection));
        tempComs.add(new PrintUniqueCommand(collection));
        tempComs.add(new RemoveByIDCommand(collection));
        tempComs.add(new RemoveGreaterCommand(collection));
        tempComs.add(new ShowCommand(collection));
        tempComs.add(new UpdateCommand(collection));
        tempComs.forEach(command -> commands.put(command.getName(), command));
        // initialize the server

        // initialize logging
        logger = Logger.getLogger("ru.bitte.lab6.server");
        String logFile = String.format("server_instance_%s.txt",
                LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)))
                .replace(":", "-").replace(" ", "_");
        FileHandler logHandler = new FileHandler(logFile);
        logger.addHandler(logHandler);
    }

    public void start(int port) throws TransformerException {
        try {
            serverConnector = new ServerConnector(port);
        } catch (IOException e) {
            logger.severe("Unknown IO exception while starting server");
            throw new RuntimeException(e);
        }
        logger.info("Starting the server...");
        boolean working = true;
        while (working) {
            working = newClient();
        }
        parser.writeToFile(collection.copyCollection(), new File("collectionSavedAt"
                + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
                .replace(":", "-").replace(" ", "_")));

    }

    private boolean newClient() {
        boolean working = true;
        try {
            serverConnector.startConnection();
            logger.info("Client connected");
        } catch (IOException e) {
            logger.severe("Unknown IO exception while starting a server connection");
            throw new RuntimeException(e);
        } catch (Exception e) {
            if (e.getMessage().equals("Y")) {
                return false;
            } else {
                throw new RuntimeException(e);
            }
        }
        while (working) {
            try {
                AbstractCommandRequest commandRequest;
                try {
                    commandRequest = serverConnector.getCommand();
                } catch (ClientDisconnectedException e) {
                    logger.info("Client has disconnected by issuing the exit command");
                    working = false;
                    continue;
                } catch (EOFException e) {
                    // for unexpected client disconnects
                    logger.warning("The client connection has been closed abruptly.");
                    working = false;
                    continue;
//                } catch (StreamCorruptedException e) {
//                    logger.warning("Error reading the client request");
//
//                    try {
//                        serverConnector.sendResponse("Error");
//                    } catch (IOException x) {
//                        logger.severe("Fuck");
//                        throw new RuntimeException(e);
//                    }
//                    continue;
                } catch (IOException e) {
                    logger.severe("Unknown IO exception while getting client request");
                    throw new RuntimeException(e);
                }
                // processing the requested command
//                if (commandRequest != null) {
                logger.info("New command requested: " + commandRequest.getCommandName());
                Command command = commands.get(commandRequest.getCommandName());
                if (command instanceof ArgumentCommand) {
                    ((ArgumentCommand) command).passArgument(((ArgumentCommandRequest) commandRequest).getArgument());
                }
                if (command instanceof IDCommand) {
                    ((IDCommand) command).passID(Integer.parseInt(((ArgumentCommandRequest) commandRequest).getArgument()));
                }
                if (command instanceof ElementCommand) {
                    ((ElementCommand) command).passElement(commandRequest.getElement().build());
                }
                String commandResult = command.run();
                addToHistory(command.getName());
                logger.info(String.format("Command \"%s\" was executed successfully", command.getName()));
                try {
                    serverConnector.sendResponse(commandResult);
                } catch (IOException e) {
                    logger.severe("Unknown IO exception while sending the client response");
                    throw new RuntimeException(e);
                }
                logger.info("Response to the command " + command.getName() + " was sent");
//                }
            } catch (ElementException e) {
                logger.warning("Error with element: " + e.getMessage());
                try {
                    serverConnector.sendResponse("Element exception: " + e.getMessage());
                } catch (IOException x) {
                    System.out.println("Unknown IO exception while reporting element exception");
                    throw new RuntimeException(x);
                }
            }
        }
        try {
            serverConnector.stopConnection();
            logger.info("Client disconnected");
            return true;
        } catch (IOException e) {
            logger.severe("Unknown IO exception while closing the server connection");
            throw new RuntimeException(e);
        }
    }

    public void addToHistory(String command) {
        if (history.size() == 15) {
            // if the capacity reaches 15, remove the oldest element
            history.removeFirst();
        }
        history.add(command);
    }
}
