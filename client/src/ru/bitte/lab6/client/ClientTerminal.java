package ru.bitte.lab6.client;

import ru.bitte.lab6.AbstractCommandRequest;
import ru.bitte.lab6.ArgumentCommandRequest;
import ru.bitte.lab6.CommandRequest;
import ru.bitte.lab6.exceptions.CommandParsingException;
import ru.bitte.lab6.exceptions.ElementConstructionException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientTerminal {
    private final Scanner in = new Scanner(System.in);
    private final ClientConnector connector = new ClientConnector();

    public void start(String host, int port) {
        // starting connection
        try {
            connector.startConnection(host, port);
        } catch (IOException e) {
            System.out.println("Unknown IO exception while starting server connection");
            throw new RuntimeException(e);
        }
        boolean active = true;
        System.out.println("Welcome to lab6! See \"help\" for the list of commands.");
        while (active) {
            System.out.print("> ");
            String input = in.nextLine();
            AbstractCommandRequest commandRequest;
            try {
                commandRequest = parseCommand(input);
            } catch (CommandParsingException | ElementConstructionException e) {
                System.out.println(e.getMessage());
                continue;
            }
            if (commandRequest.getCommandName().equals("exit")) {
                active = false;
                System.out.println("Exiting...");

            } else if (commandRequest.getCommandName().equals("execute_script")) {
                try {
                    assert commandRequest instanceof ArgumentCommandRequest;
                    String argument = ((ArgumentCommandRequest) commandRequest).getArgument();
                    List<String> commands = Files.readAllLines(Paths.get(argument));
                    for (String command : commands) {
                        try {
                            AbstractCommandRequest cr = parseCommand(command);
                            if (cr.getCommandName().equals("exit")) {
                                System.out.println("Can't exit while in a script!");
                                continue;
                            } else if (cr.getCommandName().equals("execute_script")) {
                                System.out.println("Executing scripts inside of another one is prohibited");
                                continue;
                            }
                            System.out.println(sendCommand(cr));
                        } catch (CommandParsingException | ElementConstructionException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("No script with such name found. Please provide a valid file name.");
                } catch (IOException e) {
                    System.out.println("Unknown IO exception while reading file:");
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println(sendCommand(commandRequest));
            }
        }
        // closing the connection after exiting
        try {
            connector.stopConnection();
        } catch (IOException e) {
            System.out.println("Unknown IO exception while closing the connection");
            throw new RuntimeException(e.getMessage());
        }
    }

    // sending command to the server subroutine
    private String sendCommand(AbstractCommandRequest commandRequest) {
        try {
            connector.sendRequest(commandRequest);
        } catch (IOException e) {
            // todo
            System.out.println("Unknown IO exception while sending a command request");
            throw new RuntimeException(e);
        }
        try {
            return connector.receiveResponse();
        } catch (IOException e) {
            // todo
            System.out.println("Unknown IO exception while receiving command response");
            throw new RuntimeException(e);
        }
    }

    private AbstractCommandRequest parseCommand(String input) throws CommandParsingException, ElementConstructionException {
        Pattern nonArgCommand = Pattern.compile("^([a-zA-Z_]+)[^\\S\\r\\n]*$");
        Pattern argCommand = Pattern.compile("^([a-zA-Z_]+) ([\\w-.]+)[^\\S\\r\\n]*$");
        Matcher nonArgMatch = nonArgCommand.matcher(input);
        Matcher argMatch = argCommand.matcher(input);
        AbstractCommandRequest commandRequest;
        try {
            if (nonArgMatch.matches()) {
                commandRequest = new CommandRequest(nonArgMatch.group(1));
            } else if (argMatch.matches()) {
                commandRequest = new ArgumentCommandRequest(argMatch.group(1), argMatch.group(2));
                if (commandRequest.getCommandName().contains("id")) {
                    try {
                        Integer.parseInt(((ArgumentCommandRequest) commandRequest).getArgument());
                    } catch (NumberFormatException e) {
                        throw new ElementConstructionException("Error parsing the provided command ID");
                    }
                }
            } else {
                throw new ElementConstructionException("Incorrect command format. Please see \"help\" on command usage.");

            }
        } catch (ElementConstructionException e) {
            throw new ElementConstructionException(e.getMessage() + ". Please try again.");
        }
        return commandRequest;
    }
}
