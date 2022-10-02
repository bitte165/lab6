package ru.bitte.lab6.client;

import ru.bitte.lab6.AbstractCommandRequest;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ClientConnector {
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public void startConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = socket.getOutputStream();
        in = socket.getInputStream();
    }

    public void sendRequest(AbstractCommandRequest request) throws IOException {
        var out = socket.getOutputStream();
        byte[] body = objectToBytes(request);
        byte[] header = ByteBuffer.allocate(4).putInt(body.length).array();
        out.write(header);
        out.write(body);
    }

    public String receiveResponse() throws IOException {
        byte[] header = in.readNBytes(4);
        int bodySize = ByteBuffer.wrap(header).getInt();
        byte[] body = in.readNBytes(bodySize);
        return new String(body, StandardCharsets.UTF_8);
    }

    public void stopConnection() throws IOException {
        byte[] endBytes = new byte[] {0, 0, 0, 0};
        out.write(endBytes);
        in.close();
        out.close();
        socket.close();
    }

    private byte[] objectToBytes(Object object) throws IOException {
        ObjectOutputStream out;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        }
    }
}
