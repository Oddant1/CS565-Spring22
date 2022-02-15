package Client;

import Utils.*;

import java.util.Locale;
import java.util.Scanner;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver extends Thread implements MessageTypes
{
    private final NodeInfo myInfo;
    private NodeInfo successorInfo;

    public Receiver(NodeInfo initMyInfo, NodeInfo initSuccessorInfo)
    {
        myInfo = initMyInfo;
        successorInfo = initSuccessorInfo;
    }

    public void run()
    {
        ServerSocket serverSocket = null;
        boolean isRunning = true;

        // Open a server socket listening for messages from your predecessor
        try
        {
            serverSocket = new ServerSocket(myInfo.port);
        }
        catch (IOException e)
        {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error listening on port " + myInfo.port);
            System.exit(1);
        }

        // Wait for messages from the predecessor
        while (isRunning)
        {
            try
            {
                isRunning = read(serverSocket.accept());
            }
            catch (IOException e)
            {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from predecessor: IOException" + e);
                System.exit(1);
            }
        }

        // Exit 0 not return, so we also terminate the other threads
        System.exit(0);
    }

    private boolean read(Socket serverSocket) throws IOException
    {
        Message message = null;
        ObjectInputStream fromServer = new ObjectInputStream(serverSocket.getInputStream());

        // Read the message the server sent
        try
        {
            message = (Message) fromServer.readObject();
        }
        catch (ClassNotFoundException e)
        {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error receiving message from predecessor: ClassNotFoundException" + e);
            System.exit(1);
        }

        System.out.println(message.note);

        if (message.type == JOIN && message.successor.ip.equals(myInfo.ip) && message.successor.port == myInfo.port)
        {
            successorInfo.name = message.origin.name;
            successorInfo.ip = message.origin.ip;
            successorInfo.port = message.origin.port;
        }

        // If my successor isn't the origin of the message, forward the message
        if (!successorInfo.ip.equals(message.origin.ip) && successorInfo.port != message.origin.port)
        {
            forward(message);
        }

        // If someone sent a shutdown all the message propagated will be of type SHUTDOWN_ALL
        return message.type != MessageTypes.SHUTDOWN_ALL;
    }

    private void forward(Message receivedMessage)
    {
        try
        {
            Socket socket = new Socket(successorInfo.ip, successorInfo.port);
            ObjectOutputStream toSuccessor = new ObjectOutputStream(socket.getOutputStream());
            toSuccessor.writeObject(receivedMessage);
        }
        catch (IOException e)
        {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error forwarding message to successor: IOException" + e);
            System.exit(1);
        }
    }
}
