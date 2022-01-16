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
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error starting server on port " + PORT);
            System.exit(1);
        }

        connectedClients = new ArrayList<NodeInfo>();
    }

    public void runServerLoop() throws IOException {
        boolean isRunning = true;
        System.out.println("Chat server started");

        while (isRunning) {
            System.out.println("Receiving messages on port #" + PORT);

            try {
                isRunning = handleClient(serverSocket.accept());
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from client: IOException" + e);
            } catch (ClassNotFoundException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from client: ClassNotFoundException" + e);
            }
        }
    }

    public boolean handleClient(Socket clientSocket) throws IOException, ClassNotFoundException {
        Message received = null;
        NodeInfo receivedInfo = null;
        Message toSend = null;

        DataInputStream fromClient = new DataInputStream(clientSocket.getInputStream());

        ObjectInputStream fromClientObj = new ObjectInputStream(fromClient);
        ObjectOutputStream toClientObj = null;

        received = (Message) fromClientObj.readObject();
        switch (received.type) {
            case JOIN:
                receivedInfo = (NodeInfo) received.content;
                connectedClients.add(receivedInfo);
                toSend = new Message("Welcome to the server " + receivedInfo.name, MessageTypes.NOTE);
                break;
            case NOTE:
                toSend = new Message(received.content, MessageTypes.NOTE);
                break;
            case LEAVE:
            case SHUTDOWN:
                toSend = new Message(receivedInfo.name + " has left the server", MessageTypes.NOTE);
                connectedClients.remove((NodeInfo) received.content);
                break;
            case SHUTDOWN_ALL:
                toSend = new Message("Shutdown all initiated by " + receivedInfo.name, MessageTypes.SHUTDOWN);
                break;
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket to client");
        }

        if (toSend != null) {
            for (NodeInfo nodeInfo : connectedClients) {
                send(nodeInfo, toSend);
            }
        }

        if (received.type == MessageTypes.SHUTDOWN_ALL) {
            return false;
        }

        return true;
    }

    public void send(NodeInfo nodeInfo, Message message) throws IOException {
        Socket clientSocket = new Socket(nodeInfo.ip, nodeInfo.port);
        DataOutputStream toClient = new DataOutputStream(clientSocket.getOutputStream());
        ObjectOutputStream toClientObj = new ObjectOutputStream(toClient);

        toClientObj.writeObject(message);
    }

    public static void main(String args[]) throws Exception {
        Server server = new Server();
        server.runServerLoop();
    }
}