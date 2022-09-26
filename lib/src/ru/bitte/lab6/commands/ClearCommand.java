package ru.bitte.lab6.commands;

import ru.bitte.lab6.server.CollectionKeeper;

import java.util.Scanner;

/**
 * An object of this class is used in {@code Terminal} as a command that removes every element in the maintained
 * collection. Before clearing the collection, the used will be prompted with confirmation of clearing the collection.
 * The object of this class is used by running the {@code run()} method.
 * @implNote A no-argument command
 */
public class ClearCommand extends Command {
    private final CollectionKeeper collection;

    /**
     * Constructs a {@code ClearCommand} object.
     * @param collection the reference to a collection keeper of elements
     */
    public ClearCommand(CollectionKeeper collection) {
        super("clear", "clear the collection");
        this.collection = collection;
    }

    @Override
    public String run() {
        StringBuilder output = new StringBuilder();
        collection.clearCollection();
        output.append("Successfully cleared the collection.");
//        Scanner in = new Scanner(System.in);
//        output.append("Are you sure you want to do this? [Y/n]: ");
//        String response = in.nextLine();
//        if (response.equals("Y")) {
//
//            output.append("Successfully cleared the collection.");
//        } else {
//            output.append("Canceled the command.");
//        }
        return output.toString();
    }
}
