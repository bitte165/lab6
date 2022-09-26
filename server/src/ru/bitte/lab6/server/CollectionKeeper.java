package ru.bitte.lab6.server;

import ru.bitte.lab6.exceptions.GetByIDException;
import ru.bitte.lab6.route.Route;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An object of this class holds a collection of elements and provides an interface for accessing and modifying them.
 * The collection is a {@link HashSet} containing objects of the {@link Route} class. An instance of this class can be
 * created with a collection of initial {@code Route} objects that get put into the collection first.
 */
public class CollectionKeeper {
    private final HashSet<Route> collection; // main collection
    private final LocalDateTime creationDate;

    /**
     * Returns an instance of the {@code CollectionKeeper} class.
     * @param collection the initial objects to be put in the collection
     */
    public CollectionKeeper(Collection<Route> collection){
        this.creationDate = LocalDateTime.now();
        this.collection = new HashSet<>(collection);
    }

    /**
     * Adds a new element to the collection.
     * @param element the {@code Route} to be put in the collection
     */
    public void addElement(Route element) {
        collection.add(Objects.requireNonNull(element));
    }

    /**
     * Removes an existing element from the collection.
     * @param element the element to be removed from the collection
     */
    public void removeElement(Route element) {
        collection.remove(Objects.requireNonNull(element));
    }

    /**
     * Removes all the elements in the collection.
     */
    public void clearCollection() {
        collection.clear();
    }

    /**
     * Returns a copy of the existing collection.
     * @implNote The returned copy is a shallow one, but that isn't an issue since {@code Route} objects are immutable
     * @return a copy of the collection
     */
    public HashSet<Route> copyCollection() {
        return new HashSet<>(collection);
    }

    /**
     * Returns the name of the class of objects in the collection ({@code Route}).
     * @return the string containing the collection objects class
     */
    public String getCollectionType() {
        return Route.class.toString();
    }

    /**
     * Returns the {@link LocalDateTime} object representing the collection creation date.
     * @return the {@link LocalDateTime} object representing the collection creation date
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the number of elements in the collection.
     * @return the integer number of elements in the collection
     */
    public int getCollectionSize() {
        return collection.size();
    }

    /**
     * Returns a {@link TreeSet} containing the elements from the collection.
     * @return a {@link TreeSet} containing the elements from the collection
     */
    public TreeSet<Route> copySorted() {
        return new TreeSet<>(collection);
    }

    /**
     * Returns an element from the collection with the given ID.
     * @param id the unique identification number of a wanted element in the collection
     * @return the element from the collection by the given ID
     * @throws GetByIDException if no element with such an ID was found in the collection
     */
    public Route getByID(int id) throws GetByIDException {
        Route wantedElement = null;
        for (Route route : collection) {
            if (route.getId() == id) {
                wantedElement = route;
                break;
            }
        }
        if (wantedElement == null) {
            throw new GetByIDException("No Route object with such an ID in the collection");
        } else {
            return wantedElement;
        }
    }

    /**
     * Removes an element with the given ID from the collection.
     * @param id the unique identification number of a wanted element in the collection
     * @throws GetByIDException if no element with such an ID was found in the collection
     */
    public void removeByID(int id) throws GetByIDException {
        removeElement(getByID(id));
    }

    /**
     * Replaces an element of a specific ID by the provided element of the same ID. The specific ID is the one of the
     * element from the parameter.
     * @param element the element with a specific ID that replaces an element in the collection with that ID
     * @throws GetByIDException if no element with such an ID was found in the collection
     */
    public void replaceByID(Route element) throws GetByIDException {
        int id = element.getId();
        Route oldElement = getByID(id);
        removeElement(oldElement);
        addElement(element);
    }

    /**
     * Returns a list of the elements from the collection that have a particular substring in the name.
     * @param filter the string that must be contained in the names of the returned objects
     * @return an {@link ArrayList} of the filtered elements
     */
    public List<Route> filterByString(String filter) {
        Stream<Route> elements = collection.stream();
        return elements.filter(route -> !(route.getName().contains(filter))).collect(Collectors.toList());
    }

    /**
     * Returns a list of the elements from the collection that have greater {@code distance} fields than that of the
     * provided element.
     * @param element the element the distance is compared to other elements
     * @return an {@link ArrayList} of the greater distance elements
     */
    public List<Route> getElementsGreaterThan(Route element) {
        Stream<Route> elements = collection.stream();
        return elements.filter(route -> route.getDistance() > element.getDistance()).collect(Collectors.toList());
    }
}
