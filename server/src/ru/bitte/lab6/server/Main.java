package ru.bitte.lab6.server;

import org.xml.sax.SAXException;
import ru.bitte.lab6.exceptions.ElementParsingInFileException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String usage = "Usage: server.jar -f [collection file] -p [port]";
        String fileName = null;
        int port = 0;
        if (args[0].equals("-f") && args[2].equals("-p") && args.length == 4) {
            try {
                fileName = args[1];
                port = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                System.out.println(usage);
                System.exit(0);
            }
        } else {
            System.out.println(usage);
            System.exit(0);
        }
        try {
            Server server = new Server(fileName);
            server.start(port);
        } catch (ElementParsingInFileException e) {
            System.out.println("Problem parsing objects from the file: " + e.getMessage());
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            System.out.println("Configuration error: " + e.getMessage());
        } catch (IOException | SAXException e) {
            System.out.println("Error reading from a file: " + e.getMessage());
        } catch (TransformerException e) {
            System.out.println(e.getMessage());
        }
    }
}
