import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;

public class Server {
    private final static int PORT = 23657;

    private static ServerSocket serverSocket;
    ArrayList<NodeInfo> connectedClients;

    public Server() {
        try {
            Server.serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error starting server on port " + PORT);
            System.exit(1);
        }

        connectedClients = new ArrayList<NodeInfo>();
    }

    public void runServerLoop() throws IOException {
        System.out.println("Chat server started");

        while (true) {
            System.out.println("Receiving messages on port #" + PORT);

            try {
                handleClient(serverSocket.accept());
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from client: IOException" + e);
            } catch (ClassNotFoundException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from client: ClassNotFoundException" + e);
            }
        }
    }

    public void handleClient(Socket clientSocket) throws IOException, ClassNotFoundException {
        Message received;
        Message toSend;

        DataInputStream fromClient;
        DataOutputStream toClient;

        ObjectInputStream fromClientObj;
        ObjectOutputStream toClientObj;

        try {
            System.out.println("FIRST");
            fromClient = new DataInputStream(clientSocket.getInputStream());
            System.out.println("SECOND");
            fromClientObj = new ObjectInputStream(fromClient);

            System.out.println("THIRD");
            toClient = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("FOURTH");
            toClientObj = new ObjectOutputStream(toClient);
            System.out.println("DONE");
        } catch (IOException e) {
            System.err.println("Error opening network streams ");
            return;
        }

        received = (Message) fromClientObj.readObject();
        System.out.println(((NodeInfo )received.content).name);

        switch (received.type) {
            case JOIN:
                connectedClients.add((NodeInfo) received.content);
                break;
            case NOTE:
                break;
            case LEAVE:
            case SHUTDOWN:
                connectedClients.remove((NodeInfo) received.content);
                break;
            case SHUTDOWN_ALL:
                break;
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket to client");
        }
    }

    public static void main(String args[]) throws Exception {
        Server server = new Server();
        server.runServerLoop();
    }
}