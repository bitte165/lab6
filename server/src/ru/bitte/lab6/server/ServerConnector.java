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
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

public class ServerConnector {
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private final int port;
    private final Scanner in;

    public ServerConnector(int port) throws IOException {
        this.port = port;
        in = new Scanner(System.in);
    }

    public void startConnection() throws Exception {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        selector = Selector.open();
        serverSocket.register(selector, serverSocket.validOps(), null);
        while (true) {
            System.out.print("Should I exit [Y]? (Only works if no client has connected): ");
            String response = in.nextLine();
            if (response.equals("Y")) {
                throw new Exception("Y");
            }
            int count = selector.select();
            if (count == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            for (var iter = keys.iterator(); iter.hasNext();) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    return;
                }
            }
        }
    }

    public AbstractCommandRequest getCommand() throws IOException, ClientDisconnectedException {
        while (true) {
            int count = selector.select();
            if (count == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            for (var iter = keys.iterator(); iter.hasNext(); ) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer header = ByteBuffer.allocate(4);
                    channel.read(header);
                    int bodySize = header.getInt(0);
                    if (bodySize == 0 && channel.socket().getInputStream().available() == 0) {
                        throw new ClientDisconnectedException("Client has exited");
                    }
                    ByteBuffer body = ByteBuffer.allocate(bodySize);
                    channel.read(body);
                    byte[] bodyBytes = body.array();
                    while (Arrays.equals(bodyBytes, new byte[bodyBytes.length])) {
                        body.clear();
                        channel.read(body);
                    }
                    return (AbstractCommandRequest) bytesToObject(bodyBytes);
                }
            }
        }
    }

    public void sendResponse(String str) throws IOException {
        while (true) {
            int count = selector.select();
            if (count == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            for (var iter = keys.iterator(); iter.hasNext(); ) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isValid() && key.isWritable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    byte[] stringBytes = str.getBytes(StandardCharsets.UTF_8);
                    ByteBuffer stringHeader = ByteBuffer.allocate(4).putInt(stringBytes.length);
                    stringHeader.flip();
                    channel.write(stringHeader);
                    channel.write(ByteBuffer.wrap(stringBytes));
                    return;
                }
            }
        }
    }

    public void stopConnection() throws IOException {
        selector.close();
        serverSocket.close();
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