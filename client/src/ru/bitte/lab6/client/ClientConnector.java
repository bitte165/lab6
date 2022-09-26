package ru.bitte.lab6.client;

import ru.bitte.lab6.CommandRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ClientConnector {
    private InetSocketAddress address;
    private SocketChannel serverChannel;
    private Selector selector;

    public ClientConnector(String host, int serverPort) {
        address = new InetSocketAddress(host, serverPort);
    }

    public void startConnection() {
        try {
            selector = Selector.open();
            serverChannel = SocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            serverChannel.connect(address);
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext();) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isValid() && key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        return;
                    }

                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRequest(CommandRequest request) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(request);
            ByteBuffer outBuffer = ByteBuffer.wrap(bos.toByteArray());
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext();) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isValid() && key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.write(outBuffer);
                        if (outBuffer.remaining() < 1) {
                            return;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String receiveResponse() {
        try {
            while (true) {
                selector.select();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                for (Iterator<SelectionKey> iter = selectedKeys.iterator(); iter.hasNext();) {
                    SelectionKey selectionKey = iter.next();
                    if (selectionKey.isReadable()) {
                        SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
                        StringBuilder message = new StringBuilder();
                        while (clientSocketChannel.read(buffer) > 0){
                            message.append(new String(buffer.array(), 0, buffer.position()));
                            buffer.compact();
                        }
                        return message.toString();
                    }
                    iter.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("c");
        }
        return null;
    }

    public void stopConnection() {
        try {
            serverChannel.close();
        } catch (IOException e) {
            System.out.println("well");
        }
    }
}
