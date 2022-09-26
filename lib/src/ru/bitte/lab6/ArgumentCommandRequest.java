package ru.bitte.lab6;

import ru.bitte.lab6.exceptions.CommandParsingException;
import ru.bitte.lab6.exceptions.ElementConstructionException;

import java.io.Serializable;
import java.util.Set;

public class ArgumentCommandRequest extends CommandRequest implements Serializable {
    private final String argument;
    private static final Set<String> argComamnds = Set.of("update", "remove_by_id", "execute_script", "filter_contains_name");

    public ArgumentCommandRequest(String name, String argument) throws ElementConstructionException, CommandParsingException {
        super(verifyName(name));
        this.argument = argument;
    }

    private static String verifyName(String name) throws CommandParsingException {
        if (argComamnds.contains(name)) {
            return name;
        } else if (CommandRequest.nonArgCommands.contains(name)) {
            throw new CommandParsingException("Incorrect command usage. Please see \"help\" on command usage.");
        } else {
            throw new CommandParsingException("Unknown command. Please see \"help\" for the list of commands.");
        }
    }

    public String getArgument() {
        return argument;
    }
}
