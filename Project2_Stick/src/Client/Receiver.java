package Client;

import Utils.*;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver extends Thread implements MessageTypes, Directions
{
    private final NodeInfo myInfo;
    private final NodeInfo predecessorInfo;
    private final NodeInfo successorInfo;

    public Receiver(NodeInfo initMyInfo, NodeInfo initPredecessorInfo, NodeInfo initSuccessorInfo)
    {
        myInfo = initMyInfo;
        predecessorInfo = initPredecessorInfo;
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
                    successorInfo.syncWrite(incomingMessage.other);
                }
                else
                {
                    toSend = incomingMessage;
                }

                break;
            // Ensure we don't have the same node as both predecessor and successor
            case UPDATE_PRED:
                if (!incomingMessage.other.equals(successorInfo))
                {
                    predecessorInfo.syncWrite(incomingMessage.other);
                }

                break;
            case UPDATE_SUCC:
                if (!incomingMessage.other.equals(predecessorInfo))
                {
                    successorInfo.syncWrite(incomingMessage.other);
                }
        }

        // Don't send the message to yourself if you are at the end of the stick in that direction
        if (toSend != null && ((toSend.direction == SUCCESSOR && !successorInfo.equals(myInfo))
                || (toSend.direction == PREDECESSOR && !predecessorInfo.equals(myInfo))))
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

        // Tell our current successor to change their predecessor
        toSend = new Message(myInfo, incomingMessage.origin, "A new node has joined, sending your new predecessor info.", UPDATE_PRED, SUCCESSOR);
        send(toSend);

        // Update your successor to be the joining node
        successorInfo.syncWrite(incomingMessage.origin);

        // Tell the joining node to update its info
        toSend = new Message(myInfo, oldSuccessorInfo, "Sending your new successor info", UPDATE_SUCC, SUCCESSOR);

        // Send updated info to the joining node
        send(toSend);


//        send(new Message(myInfo, successorInfo, incomingMessage.note, NOTE));
    }

    private void send(Message toSend)
    {
        Socket socket = null;
        ObjectOutputStream toNeighbor;

        try
        {
            if (toSend.direction == PREDECESSOR && !predecessorInfo.equals(myInfo))
            {
                socket = new Socket(predecessorInfo.ip, predecessorInfo.port);
            }
            else if (toSend.direction == SUCCESSOR && !successorInfo.equals(myInfo))
            {
                socket = new Socket(successorInfo.ip, successorInfo.port);
            }

            if (socket != null)
            {
                toNeighbor = new ObjectOutputStream(socket.getOutputStream());
                toNeighbor.writeObject(toSend);
            }
        }
        catch (IOException e)
        {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error sending message: IOException " + e);
            System.exit(1);
        }
    }
}
