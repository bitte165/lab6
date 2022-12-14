package ru.bitte.lab6.commands;

import ru.bitte.lab6.server.CollectionKeeper;
import ru.bitte.lab6.route.Route;

import java.util.List;

/**
 * An object of this class is used in {@code Terminal} as a command that the elements of the collection the distance of
 * which is greater than the distance of the provided element. The object of this class is used by supplying a
 * {@code Route} object through the method of the parent {@link ElementCommand} class and then running by
 * the {@code run()} method.
 * @implNote An element command
 */
public class RemoveGreaterCommand extends ElementCommand {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code RemoveGreaterCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public RemoveGreaterCommand(CollectionKeeper collection) {
        super("remove_greater", "remove all of the collection elements " +
                "the distance of which exceeds the given element's");
        this.collection = collection;
    }

    @Override
    public String run() {
        StringBuilder output = new StringBuilder();
//        Scanner in = new Scanner(System.in);
        // gets the elements the distance of which is greater than of the provided one
        List<Route> greaterElements = collection.getElementsGreaterThan(getElement());
        int n = greaterElements.size();
        for (Route element : collection.copyCollection()) {
            if (greaterElements.contains(element)) {
                collection.removeElement(element);
            }
        }
        return String.format("Successfully removed %d elements.", n);
    }
}

