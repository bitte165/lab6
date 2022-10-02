package ru.bitte.lab6;

import ru.bitte.lab6.exceptions.CommandParsingException;
import ru.bitte.lab6.exceptions.ElementConstructionException;

import java.io.Serializable;
import java.util.Set;

public class ArgumentCommandRequest extends AbstractCommandRequest implements Serializable {
    private final String argument;

    protected static final Set<String> argComamnds = Set.of("update", "remove_by_id", "execute_script", "filter_contains_name");

    public ArgumentCommandRequest(String name, String argument) throws ElementConstructionException, CommandParsingException {
        super(name);
        if (CommandRequest.nonArgCommands.contains(name)) {
            throw new CommandParsingException("Incorrect command usage. Please see \"help\" on command usage.");
        } else if (!argComamnds.contains(name)) {
            throw new CommandParsingException("Unknown command. Please see \"help\" for the list of commands.");
        } else {
            assert false;
        }
        if (CommandRequest.elementCommands.contains(name)) {
            setElement(CommandRequest.generateElement());
        }
        this.argument = argument;
    }

    public String getArgument() {
        return argument;
    }
}
