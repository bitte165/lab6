package ru.bitte.lab6.client;

import ru.bitte.lab6.ArgumentCommandRequest;
import ru.bitte.lab6.CommandRequest;
import ru.bitte.lab6.exceptions.CommandParsingException;
import ru.bitte.lab6.exceptions.ElementConstructionException;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientTerminal {
    private final Scanner in;
    private ClientConnector connector;

    public ClientTerminal(String host, int serverPort) {
        in = new Scanner(System.in);
        connector = new ClientConnector(host,serverPort);
    }

    public void start() throws ElementConstructionException, CommandParsingException {
        connector.startConnection();
        boolean active = true;
        Pattern nonArgCommand = Pattern.compile("^([a-zA-Z_]+)[^\\S\\r\\n]*$");
        Pattern argCommand = Pattern.compile("^([a-zA-Z_]+) ([\\w-.]+)[^\\S\\r\\n]*$");
        System.out.println("Welcome to lab6! See \"help\" for the list of commands.");
        while (active) {
            System.out.print("> ");
            String input = in.nextLine();
            Matcher nonArgMatch = nonArgCommand.matcher(input);
            Matcher argMatch = argCommand.matcher(input);
            CommandRequest commandRequest;
            if (nonArgMatch.matches()) {
                commandRequest = new CommandRequest(nonArgMatch.group(1));
            } else if (argMatch.matches()) {
                commandRequest = new ArgumentCommandRequest(nonArgMatch.group(1), nonArgMatch.group(2));
            } else {
                System.out.println("Incorrect command format. Please see \"help\" on command usage.");
                continue;
            }
            if (commandRequest.getCommandName().equals("exit")) {
                active = false;
            } else if (commandRequest.getCommandName().equals("execute_script")) {
                // todo
                System.out.println("suka");
            } else {
                connector.sendRequest (commandRequest);
                System.out.print(connector.receiveResponse());
            }
        }
    }
}
