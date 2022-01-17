import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.EnumMap;

public class Server {
    private final static int PORT = 23657;

    private static ServerSocket serverSocket;
    ArrayList<NodeInfo> connectedClients;

    private EnumMap<MessageTypes, String> typeToStr = new EnumMap<MessageTypes, String>(MessageTypes.class);

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error starting server on port " + PORT);
            System.exit(1);
        }

        typeToStr.put(MessageTypes.JOIN, "JOIN");
        typeToStr.put(MessageTypes.LEAVE, "LEAVE");
        typeToStr.put(MessageTypes.SHUTDOWN, "SHUTDOWN");
        typeToStr.put(MessageTypes.SHUTDOWN_ALL, "SHUTDOWN_ALL");

        connectedClients = new ArrayList<NodeInfo>();
    }

    public void runServerLoop() {
        boolean isRunning = true;
        System.out.println("Chat server started");
        System.out.println("Receiving messages on port #" + PORT + "\n");

        while (isRunning) {
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

        received = (Message) fromClientObj.readObject();
        if (received.type != MessageTypes.NOTE) {
            receivedInfo = (NodeInfo) received.content;
            System.out.println("Received " + typeToStr.get(received.type) + " command from " + receivedInfo.name);
        } else {
            System.out.println("Received note: \"" + received.content + "\"");
        }

        switch (received.type) {
            case JOIN:
                connectedClients.add(receivedInfo);
                toSend = new Message(receivedInfo.name + " has joined the server.", MessageTypes.NOTE);
                break;
            case NOTE:
                toSend = new Message(received.content, MessageTypes.NOTE);
                break;
            case LEAVE:
            case SHUTDOWN:
                toSend = new Message(receivedInfo.name + " has left the server.", MessageTypes.NOTE);
                removeClient(receivedInfo);
                break;
            case SHUTDOWN_ALL:
                toSend = new Message("Shutdown all initiated by " + receivedInfo.name + ".", MessageTypes.SHUTDOWN);
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

    public void removeClient(NodeInfo receivedInfo) {
        NodeInfo tempNode;

        for (int i = 0; i < connectedClients.size(); i++) {
            tempNode = connectedClients.get(i);

            if (tempNode.name.equals(receivedInfo.name)
                    && tempNode.ip.equals(receivedInfo.ip)
                    && tempNode.port == receivedInfo.port) {
                connectedClients.remove(i);
                return;
            }
        }
    }

    public static void main(String args[]) {
        Server server = new Server();
        server.runServerLoop();
    }
}