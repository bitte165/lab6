package ru.bitte.lab6.server;

import ru.bitte.lab6.AbstractCommandRequest;
import ru.bitte.lab6.exceptions.ClientDisconnectedException;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ServerConnector {
    private ServerSocketChannel serverSocket;
    private SocketChannel clientSocket;

    public ServerConnector(int port) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
    }

    public void startConnection() throws IOException {
        clientSocket = serverSocket.accept();
    }

    public AbstractCommandRequest getCommand() throws IOException, ClientDisconnectedException {
        ByteBuffer header = ByteBuffer.allocate(4);
        clientSocket.read(header);
        int bodySize = header.getInt(0);
        if (bodySize == 0 && clientSocket.socket().getInputStream().available() == 0) {
            throw new ClientDisconnectedException("Client has exited");
        }
        ByteBuffer body = ByteBuffer.allocate(bodySize);
        clientSocket.read(body);
        return (AbstractCommandRequest) bytesToObject(body.array());
    }

    public void sendResponse(String str) throws IOException {
        byte[] stringBytes = str.getBytes(StandardCharsets.UTF_8);
        ByteBuffer stringHeader = ByteBuffer.allocate(4).putInt(stringBytes.length);
        stringHeader.flip();
        clientSocket.write(stringHeader);
        clientSocket.write(ByteBuffer.wrap(stringBytes));
    }

    public void stopConnection() throws IOException {
        clientSocket.close();
    }

    private Object bytesToObject(byte[] bytes) throws IOException {
        ObjectInputStream out;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)){
            out = new ObjectInputStream(bis);
            return out.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}