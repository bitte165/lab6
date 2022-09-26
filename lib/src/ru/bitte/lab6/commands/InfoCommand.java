package ru.bitte.lab6.commands;

import ru.bitte.lab6.server.CollectionKeeper;
import ru.bitte.lab6.route.Route;

import java.util.Collections;
import java.util.Set;

/**
 * An object of this class is used in {@code Terminal} as a command that outputs information about the current state
 * of the maintained collection. The information includes the type of the collection's elements, the creation date, the
 * current number of elements in it, the maximum and the minimum distances among the elements in the collection.
 * The object of this class is used by running the {@code run()} method.
 * @implNote A no-argument command
 */
public class InfoCommand extends Command {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code InfoCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public InfoCommand(CollectionKeeper collection) {
        super("info", "output information about the collection");
        this.collection = collection;
    }

    @Override
    public String run() {
        StringBuilder output = new StringBuilder();
        Set<Route> elements = collection.copySorted();
        output.append("Information about this collection:\n");
        output.append("Type: " + collection.getCollectionType() + "\n");
        output.append("Creation date: " + collection.getCreationDate().toString() + "\n");
        output.append("Number of elements: " + collection.getCollectionSize() + "\n");
        output.append("Max distance: " + Collections.max(elements).toString() + "\n");
        output.append("Min distance: " + Collections.min(elements).toString() + "\n");
        return output.toString();
    }
}
