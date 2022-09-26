package ru.bitte.lab6.commands;

import ru.bitte.lab6.server.CollectionKeeper;
import ru.bitte.lab6.exceptions.*;
import ru.bitte.lab6.route.Route;

/**
 * An object of this class is used in {@code Terminal} as a command that updates an element by the provided ID from the
 * maintained collection by replacing the fields provided by the passed element. As such, the ID and creation date are
 * left unmodified, only the new field values are replaced. The object of this class is used by supplying an ID of
 * an element from the collection through the {@code passID(int)} method, a {@code Route} object through the method of
 * the parent {@link ElementCommand} class and then running by the {@code run()} method.
 * @see ElementCommand#passElement(Route)
 * @implNote An ID-element command
 */
public class UpdateCommand extends ElementCommand implements IDCommand {
    private final CollectionKeeper collection;
    private int id;

    /**
     * Constructs a {@code UpdateCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public UpdateCommand(CollectionKeeper collection) {
        super("update", "update the values of the collection element provided by the ID");
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
        Route varElement;
        // tries to retrieve an object by the id and if fails
        try {
            // get element by id, if no element is found an exception is called
            varElement = collection.getByID(id);
            Route newFields = getElement();
            assert varElement != null;
            // modifying the retrieved element with new fields if needed
            if (!varElement.getName().equals(newFields.getName())) {
                varElement = varElement.changeName(newFields.getName());
            }
            if (!varElement.getCoordinates().equals(newFields.getCoordinates())) {
                varElement = varElement.changeCoordinates(newFields.getCoordinates());
            }
            if (!varElement.getFrom().equals(newFields.getFrom())) {
                varElement = varElement.changeFrom(newFields.getFrom());
            }
            if (!varElement.getTo().equals(newFields.getTo())) {
                varElement = varElement.changeTo(newFields.getTo());
            }
            // replacing with the new modified object
            collection.replaceByID(varElement);
            return "The element by ID " + getID() + " has been updated successfully.";
        } catch (GetByIDException e){
            return "No element with such an ID found.";
        } catch (ElementConstructionException e){
            return "Couldn't change the object because: " + e.getMessage() + "\n" +
            "The element by the provided ID was left intact.";
        }
    }
}
