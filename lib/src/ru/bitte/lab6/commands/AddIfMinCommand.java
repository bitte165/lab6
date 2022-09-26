package ru.bitte.lab6.commands;

import ru.bitte.lab6.server.CollectionKeeper;
import ru.bitte.lab6.route.Route;

import java.util.Collections;
import java.util.Objects;

/**
 * An object of this class is used in {@code Terminal} as a command that creates and adds a new {@link Route} object to
 * the maintained collection of objects if the object's distance value is less than the minimum distance value in the
 * collection. The object of this class is used by supplying a {@code Route} object through the method of the parent
 * {@link ElementCommand} class and then running by the {@code run()} method.
 * @see ElementCommand#passElement(Route)
 * @implNote An element command
 */
public class AddIfMinCommand extends ElementCommand {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code AddIfMinCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public AddIfMinCommand(CollectionKeeper collection) {
        super("add_if_min", "add a new element to the collection if its value is less than " +
                "that of the minimum element in the collection");
        this.collection = collection;
    }

    @Override
    public String run() {
        StringBuilder output = new StringBuilder();
        Route min = Collections.min(collection.copySorted());
        if (getElement().getDistance() < min.getDistance()) {
            collection.addElement(Objects.requireNonNull(getElement()));
            output.append(String.format("Added %s to the collection\n", getElement().getName()));
        } else {
            output.append("Didn't add the element since its distance value was greater than " +
                    "the minimum one in the collection");
        }
        return output.toString();
    }
}
