package Client;

import Utils.*;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver extends Thread implements MessageTypes
{
    private final NodeInfo myInfo;
    private final NodeInfo successorInfo;

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
        Message incomingMessage = null;
        ObjectInputStream fromOther = new ObjectInputStream(serverSocket.getInputStream());

        // Read the message the server sent
        try
        {
            incomingMessage = (Message) fromOther.readObject();
        }
        catch (ClassNotFoundException e)
        {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error receiving message: ClassNotFoundException" + e);
            System.exit(1);
        }

        // Report the note from the incoming message
        System.out.println(incomingMessage.origin.name + ": " + incomingMessage.note);
        // Handle the message appropriately
        handle(incomingMessage);

        // If someone sent a shutdown all the message propagated will be of type SHUTDOWN_ALL, and we want to exit
        return incomingMessage.type != MessageTypes.SHUTDOWN_ALL;
    }

    private void handle(Message incomingMessage)
    {
        Message toSend = null;

        switch (incomingMessage.type)
        {
            case JOIN:
                // Deal with adding the node to the chat
                handleJoin(incomingMessage);
                break;
            case NOTE:
                toSend = incomingMessage;
                break;
            case LEAVE:
            case SHUTDOWN:
            case SHUTDOWN_ALL:
                if (incomingMessage.origin.equals(successorInfo))
                {
                    successorInfo.syncWrite(incomingMessage.other.name, incomingMessage.other.ip, incomingMessage.other.port);
                }
                else
                {
                    toSend = incomingMessage;
                }

                break;
            case UPDATE:
                successorInfo.syncWrite(incomingMessage.other.name, incomingMessage.other.ip, incomingMessage.other.port);
        }

        // If my successor isn't the origin of the message, and if we have a message to send, forward the message
        if (!successorInfo.equals(incomingMessage.origin) && toSend != null)
        {
            send(toSend);
        }
    }

    // Join is a sort of special case message that requires special handling
    private void handleJoin(Message incomingMessage)
    {
        // Create message to send to joining node
        Message toSend;
        NodeInfo oldSuccessorInfo = new NodeInfo(successorInfo);

        // Update your successor to be the joining node
        successorInfo.syncWrite(incomingMessage.origin.name, incomingMessage.origin.ip,
                                incomingMessage.origin.port);

        // Tell the joining node to update its info
        toSend = new Message(myInfo, oldSuccessorInfo, "Sending your new successor info", UPDATE);

        // Send updated info to the joining node
        send(toSend);
        send(new Message(myInfo, successorInfo, incomingMessage.note, NOTE));
    }

    private void send(Message toSend)
    {
        try
        {
            Socket socket = new Socket(successorInfo.ip, successorInfo.port);
            ObjectOutputStream toSuccessor = new ObjectOutputStream(socket.getOutputStream());
            toSuccessor.writeObject(toSend);
        }
        catch (IOException e)
        {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error sending message: IOException " + e);
            System.exit(1);
        }
    }
}
