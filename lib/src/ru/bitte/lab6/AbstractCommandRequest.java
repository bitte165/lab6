package ru.bitte.lab6;

import ru.bitte.lab6.exceptions.ElementException;
import ru.bitte.lab6.route.Route;

import java.io.Serializable;

public abstract class AbstractCommandRequest implements Serializable {
    private final String name;
    private Route.RouteBuilder element;

    public AbstractCommandRequest(String n) {
        name = n;
        element = null;
    }

    public String getCommandName() {
        return name;
    }

    public Route.RouteBuilder getElement() throws ElementException {
        if (element == null) {
            throw new ElementException("Can't get this command's element - it doesn't require one!");
        }
        return element;
    }

    protected void setElement(Route.RouteBuilder e) {
        element = e;
    }
}
