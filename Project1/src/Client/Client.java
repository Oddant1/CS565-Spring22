package Client;

import Utils.*;
import Server.*;

import java.util.Locale;
import java.util.Scanner;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

class Client {
    private String serverIp;
    private int serverPort;

    private String clientIp;
    private int clientPort;
    private String name;

    private static ServerListen serverListen = null;
    private static UserListen userListen = null;

    Client() throws java.io.IOException {
        File properties = new File("properties.txt");
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter your name: ");
        name = scanner.nextLine();
        System.out.println("Hello " + name + ". Please type \"JOIN\" to enter the server");

        try {
            scanner = new Scanner(properties);

            serverIp = scanner.nextLine();
            serverPort = Integer.parseInt(scanner.nextLine());
            clientIp = scanner.nextLine();
            clientPort = Integer.parseInt(scanner.nextLine());

            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Failed to find properties file.");
            System.exit(1);
        }

        serverListen = new ServerListen();
        userListen = new UserListen();

        serverListen.start();
        userListen.start();
    }

    class ServerListen extends Thread {
        ServerSocket serverSocket;

        public void run() {
            Message message = null;
            boolean isRunning = true;

            try {
                serverSocket = new ServerSocket(clientPort);
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error listening on port " + clientPort);
                System.exit(1);
            }

            while (isRunning) {
                try {
                    isRunning = read(serverSocket.accept());
                } catch (IOException e) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                    System.err.println("Error receiving message from client: IOException" + e);
                }
            }
        }

        public boolean read(Socket serverSocket) throws IOException {
            Message message = null;
            DataInputStream fromServer = new DataInputStream(serverSocket.getInputStream());
            ObjectInputStream fromServerObj = new ObjectInputStream(fromServer);

            try {
                message = (Message) fromServerObj.readObject();
            } catch (ClassNotFoundException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from server: ClassNotFoundException" + e);
            }

            if (message.type == MessageTypes.NOTE) {
                System.out.println((message.content));
            } else if (message.type == MessageTypes.SHUTDOWN) {
                return false;
            }

            return  true;
        }
    }

    class UserListen extends Thread {
        Socket socket;

        public void run() {
            Scanner scanner = null;
            String input;
            Message message = null;
            boolean isRunning = true;

            while (isRunning) {
                scanner = new Scanner(System.in);
                input = scanner.nextLine();
                message = parse(input);

                if (message.type == MessageTypes.SHUTDOWN ||
                        message.type == MessageTypes.SHUTDOWN_ALL) {
                    isRunning = false;
                }

                if (message.type == MessageTypes.LEAVE) {
                    System.out.println("You have left the server.");
                }

                try {
                    socket = new Socket(serverIp, serverPort);
                    send(message, socket);
                } catch (IOException e) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                    System.err.println("Failed to send message:\n" + e);
                    System.exit(1);
                }
            }

            System.out.println("Goodbye " + name);
            // Exit 0 not return so we also terminate the other thread
            System.exit(0);
        }

        public Message parse(String input) {
            NodeInfo node = new NodeInfo(clientIp, clientPort, name);
            Message message;

            if (input.toUpperCase(Locale.ROOT).equals("JOIN")) {
                message = new Message(node, MessageTypes.JOIN);
            } else if (input.toUpperCase(Locale.ROOT).equals("LEAVE")) {
                message = new Message(node, MessageTypes.LEAVE);
            } else if (input.toUpperCase(Locale.ROOT).equals("SHUTDOWN")) {
                message = new Message(node, MessageTypes.SHUTDOWN);
            } else if (input.toUpperCase(Locale.ROOT).equals("SHUTDOWN ALL")) {
                message = new Message(node, MessageTypes.SHUTDOWN_ALL);
            } else {
                message = new Message(name + ": " + input, MessageTypes.NOTE);
            }

            return message;
        }

        public void send(Message message, Socket socket) throws IOException {
            DataOutputStream toServer = null;
            ObjectOutputStream toServerObj = null;

            toServer = new DataOutputStream(socket.getOutputStream());
            toServerObj = new ObjectOutputStream(toServer);

            toServerObj.writeObject(message);
        }
    }

    public static void main(String[] args) throws Exception {
        new Client();
    }
}
