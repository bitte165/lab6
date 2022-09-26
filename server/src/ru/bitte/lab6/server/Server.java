package ru.bitte.lab6.server;

import org.xml.sax.SAXException;
import ru.bitte.lab6.ArgumentCommandRequest;
import ru.bitte.lab6.CommandRequest;
import ru.bitte.lab6.commands.*;
import ru.bitte.lab6.exceptions.ElementException;
import ru.bitte.lab6.exceptions.ElementParsingInFileException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class Server {
    private int serverPort;
    private final Scanner in;
    @SuppressWarnings("FieldCanBeLocal")
    private final CollectionKeeper collection;
    private final Parser parser = new Parser();
    private final Map<String, Command> commands;
    private final Deque<String> history;
    private final ServerConnector connector = new ServerConnector();

    public void start() throws TransformerException {
        System.out.println("Starting the server...");
        connector.startConnection(serverPort);
        boolean working = true;
        while (working) {
            newClient();
            System.out.print("No clients working right now. Exit? (y): ");
            if (in.nextLine().equals("y")) {
                working = false;
            }
        }
        parser.writeToFile(collection.copyCollection(), new File("collectionSavedAt" + LocalDateTime.now()));

    }

    public Server(String fileName, int port) throws TransformerConfigurationException, ParserConfigurationException, ElementParsingInFileException, IOException, SAXException {
        serverPort = port;
        in = new Scanner(System.in);
        // initialize the collection keeper with a file
        collection = new CollectionKeeper(parser.readFromFile(new File(fileName)));
        // initialize the commands by first getting them in a hashset and then adding to a hashmap in a loop
        history = new ArrayDeque<>(15);
        commands = new HashMap<>();
        HashSet<Command> tempComs = new HashSet<>();
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
    }

    public void newClient() {
        boolean working = true;
        while (working) {
            try {
                CommandRequest commandRequest = connector.nextCommand(2000);
                if (commandRequest != null) {
                    Logging.log(Level.INFO, "New comamnd requested: " + commandRequest.getCommandName());
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
                    Logging.log(Level.INFO, "Command " + command.getName() + " was executed without errors.");
                    if (connector.checkIfConnectionClosed()) {
                        connector.stopConnection();
                        working = false;
                    }
                    connector.sendResponse(commandResult);
                    Logging.log(Level.INFO, "Response to command " + command.getName() + "was sent.");
                }

            } catch (ElementException e) {
                Logging.log(Level.WARNING, "Error with element: " + e.getMessage());
            } catch (TimeoutException e) {
                connector.stopConnection();
                working = false;
            }
        }
        Logging.log(Level.INFO, "Client disconnected");
    }
}
