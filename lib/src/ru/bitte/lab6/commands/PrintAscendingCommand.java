package ru.bitte.lab6.commands;

import ru.bitte.lab6.server.CollectionKeeper;
import ru.bitte.lab6.route.Route;

import java.util.Set;

/**
 * An object of this class is used in {@code Terminal} as a command that prints all the elements in the maintained
 * collection in the ascending order (sorting by the distance value). The object of this class is used by running
 * the {@code run()} method.
 * @implNote A no-argument command
 */
public class PrintAscendingCommand extends Command {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code PrintAscendingCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public PrintAscendingCommand(CollectionKeeper collection) {
        super("print_ascending", "output the collection elements in the ascending order");
        this.collection = collection;
    }

    @Override
    public String run() {
        StringBuilder output = new StringBuilder();
        System.out.println("Collection elements in the ascending order:");
        Set<Route> mirror = collection.copySorted();
        for (Route element : mirror) {
            output.append(element.format()).append("\n");
        }
        return output.toString();
    }
}
