package ru.bitte.lab6.client;

import ru.bitte.lab6.exceptions.CommandParsingException;
import ru.bitte.lab6.exceptions.ElementConstructionException;

import java.util.Optional;

public class Main {
    public static void main(String[] args) throws ElementConstructionException, CommandParsingException {
        String host = Optional.ofNullable(System.getenv("HOST")).orElseThrow(() -> new RuntimeException("sex"));
        int port = Integer.parseInt(System.getenv("PORT"));
        ClientTerminal client = new ClientTerminal(host, port);
        client.start();
    }
}
