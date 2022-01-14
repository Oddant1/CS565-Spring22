import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
            fromClient = new DataInputStream(clientSocket.getInputStream());
            fromClientObj = new ObjectInputStream(fromClient);

            toClient = new DataOutputStream(clientSocket.getOutputStream());
            toClientObj = new ObjectOutputStream(toClient);
        } catch (IOException e) {
            System.err.println("Error opening network streams");
            return;
        }

        received = (Message) fromClientObj.readObject();

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
        Server Server = new Server();
        Server.runServerLoop();
    }
}