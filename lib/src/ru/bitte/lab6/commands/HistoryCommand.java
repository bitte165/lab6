package ru.bitte.lab6.commands;

import java.util.Deque;

/**
 * An object of this class is used in {@code Terminal} as a command that outputs the last 15 used commands in the current
 * terminal. The commands are printed without their arguments. The object of this class is used by running
 * the {@code run()} method.
 * @implNote A no-argument command
 */
public class HistoryCommand extends Command {
    private final Deque<String> history;

    /**
     * Constructs a {@code HistoryCommand} object.
     * @param history the list that maintains the latest run commands
     */
    public HistoryCommand(Deque<String> history) {
        super("history", "output the last 15 used commands (without their arguments)");
        this.history = history;
    }

    @Override
    public String run() {
        StringBuilder output = new StringBuilder();
        System.out.println("Last 15 used commands:");
        history.forEach((x) -> output.append(x + "\n"));
        return output.toString();
    }
}
