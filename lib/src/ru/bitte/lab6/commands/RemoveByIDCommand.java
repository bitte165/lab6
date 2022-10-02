package ru.bitte.lab6.commands;

import ru.bitte.lab6.server.CollectionKeeper;
import ru.bitte.lab6.exceptions.GetByIDException;
import ru.bitte.lab6.route.Route;

/**
 * An object of this class is used in {@code Terminal} as a command that removes an object with the provided ID from the
 * maintained collection. The object of this class is used by supplying an ID of a {@link Route} object through the
 * {@code passID(int)} method and then running by the {@code run()} method.
 * @implNote An ID command
 */
public class RemoveByIDCommand extends Command implements IDCommand {
    private final CollectionKeeper collection;
    private int id;

    /**
     * Constructs a {@code RemoveByIDCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public RemoveByIDCommand(CollectionKeeper collection) {
        super("remove_by_id", "remove an element of the specified ID from the collection");
        this.collection = collection;
    }

    @Override
    public void passID(int id) {
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String run() {
        StringBuilder output = new StringBuilder();
        try {
            collection.removeByID(id);
            output.append("Successfully removed the element by ID.").append(getID());
        } catch (GetByIDException e) {
            output.append("No element with such an ID found.");
        }
        return output.toString();
    }
}
