package ru.bitte.lab6.server;

import ru.bitte.lab6.ArgumentCommandRequest;
import ru.bitte.lab6.CommandRequest;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class ServerConnector {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private ObjectInputStream in;

    public void startConnection(int port){
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            Logging.log(Level.INFO, "Connection was accepted.");
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
        } catch (IOException e){
            Logging.log(Level.WARNING, "Exception during connection starting: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public CommandRequest nextCommand(int timeout) throws TimeoutException {
        CommandRequest result = null;
        try {
            while(result == null) {
                result = (CommandRequest) in.readObject();
                timeout--;
                if(timeout < 0){
                    throw new TimeoutException("Reading time is out");
                }
            }
        } catch (IOException | ClassNotFoundException e){
            Logging.log(Level.WARNING, "Exception during nextCommand " + e.getMessage());
            throw new RuntimeException(e);
        }
        Logging.log(Level.INFO, "Received command: " + result.getCommandName()+ " "
                + Optional.of(((ArgumentCommandRequest) result).getArgument()).orElse("") + ".");
        return result;
    }

    public void sendResponse(String str){
        out.println(str);
        Logging.log(Level.INFO, "Message '" + str + "' was sent.");
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
            Logging.log(Level.INFO, "Connection was closed.");
        } catch (IOException e){
            Logging.log(Level.WARNING, "Failed to stopConnection");
        }
    }

    public boolean checkIfConnectionClosed(){
        return serverSocket.isClosed();
    }
}