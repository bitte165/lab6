package ru.bitte.lab6.server;

import org.xml.sax.SAXException;
import ru.bitte.lab6.exceptions.ElementParsingInFileException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            String collection = System.getenv("COLLECTION");
            int port = Integer.parseInt(System.getenv("PORT"));
            Server server = new Server(collection, port);
            server.start();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (ElementParsingInFileException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
