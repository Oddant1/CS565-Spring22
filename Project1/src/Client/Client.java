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
    boolean hasJoined = false;

    private String serverIp;
    private int serverPort;

    private String clientIp;
    private int clientPort;
    private final String name;

    Client() {
        File properties = new File("properties.txt");
        Scanner scanner = new Scanner(System.in);

        // Accept a name and prompt the user to join the chat
        System.out.println("Please enter your name: ");
        name = scanner.nextLine();
        System.out.println("Hello " + name + ". Please type \"JOIN\" to enter the server");

        // Get client and server data out of properties file
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

        // Init and run our threads
        new Receiver().start();
        new Sender().start();
    }

    class Receiver extends Thread {
        ServerSocket serverSocket;

        public void run() {
            boolean isRunning = true;

            // Open a server socket listening to the server
            try {
                serverSocket = new ServerSocket(clientPort);
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error listening on port " + clientPort);
                System.exit(1);
            }

            // Wait for messages from the server
            while (isRunning) {
                try {
                    isRunning = read(serverSocket.accept());
                } catch (IOException e) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                    System.err.println("Error receiving message from client: IOException" + e);
                    System.exit(1);
                }
            }

            // Exit 0 not return, so we also terminate the other thread
            System.exit(0);
        }

        public boolean read(Socket serverSocket) throws IOException {
            Message message = null;
            ObjectInputStream fromServer = new ObjectInputStream(serverSocket.getInputStream());

            // Read the message the server sent
            try {
                message = (Message) fromServer.readObject();
            } catch (ClassNotFoundException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from server: ClassNotFoundException" + e);
                System.exit(1);
            }

            // Intellij doesn't understand that these messages are guaranteed to not be null because if they failed to
            // init we would error out.
            if (message != null) {
                System.out.println(message.content);
            }

            // If someone sent a shutdown all the server will have sent a shutdown request to the client
            return message != null && message.type != MessageTypes.SHUTDOWN;
        }
    }

    class Sender extends Thread {
        Socket socket;

        public void run() {
            Scanner scanner;
            String input;
            Message message;
            boolean isRunning = true;

            // Wait on user input
            while (isRunning) {
                scanner = new Scanner(System.in);
                input = scanner.nextLine();
                message = parse(input);

                // Ensure that this is the last iteration of the loop if we received a shutdown. If the user is
                // connected, this will remove it from the server's client list. Otherwise, it will just close
                if (message.type == MessageTypes.SHUTDOWN || message.type == MessageTypes.SHUTDOWN_ALL) {
                    isRunning = false;
                }

                // Ensure the user has joined before letting them send messages
                if (message.type == MessageTypes.JOIN) {
                    if (hasJoined) {
                        System.out.println("You have already joined the server.");
                        continue;
                    } else {
                        hasJoined = true;
                    }
                } else if (!hasJoined) {
                    if (message.type != MessageTypes.SHUTDOWN) {
                        System.out.println("You must join the server before attempting to communicate with it.");
                    }
                    continue;
                }

                // Indicate that we are not connected if we leave and inform the user they may reconnect
                if (message.type == MessageTypes.LEAVE) {
                    System.out.println("You have left the server. Type \"JOIN\" again to reconnect.");
                    hasJoined = false;
                }

                // Send their message to the client
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
            // Exit 0 not return, so we also terminate the other thread
            System.exit(0);
        }

        public Message parse(String input) {
            NodeInfo node = new NodeInfo(clientIp, clientPort, name);
            // Intellij suggested this "advanced switch." It's neat.
            return switch (input.toUpperCase(Locale.ROOT)) {
                case "JOIN" -> new Message(node, MessageTypes.JOIN);
                case "LEAVE" -> new Message(node, MessageTypes.LEAVE);
                case "SHUTDOWN" -> new Message(node, MessageTypes.SHUTDOWN);
                case "SHUTDOWN ALL" -> new Message(node, MessageTypes.SHUTDOWN_ALL);
                default -> new Message(name + ": " + input, MessageTypes.NOTE);
            };
        }

        public void send(Message message, Socket socket) throws IOException {
            ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
            toServer.writeObject(message);
        }
    }

    public static void main(String[] args) {
        // Kickstart the whole thing
        new Client();
    }
}
