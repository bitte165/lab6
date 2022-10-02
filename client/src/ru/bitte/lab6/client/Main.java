package ru.bitte.lab6.client;

public class Main {
    public static void main(String[] args) {
        String usage = "Usage: client.jar -h [host address] -p [port]";
        String hostName = null;
        int port = 0;
        if (args[0].equals("-h") && args[2].equals("-p") && args.length == 4) {
            try {
                hostName = args[1];
                port = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                System.out.println(usage);
                System.exit(0);
            }
        } else {
            System.out.println(usage);
            System.exit(0);
        }
        ClientTerminal client = new ClientTerminal();
        client.start(hostName, port);
    }
}
