package Client;

import Utils.*;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StickClientListener extends Thread implements StickMessageTypes, StickDirections
{
    private final StickNodeInfo myInfo;
    private final StickNodeInfo predecessorInfo;
    private final StickNodeInfo successorInfo;
    private final StickSender sender;

    public StickClientListener(StickNodeInfo initMyInfo, StickNodeInfo initPredecessorInfo, StickNodeInfo initSuccessorInfo)
    {
        myInfo = initMyInfo;
        predecessorInfo = initPredecessorInfo;
        successorInfo = initSuccessorInfo;
        sender = new StickSender();
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
            Logger.getLogger(StickClientListener.class.getName()).log(Level.SEVERE, null, e);
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
                Logger.getLogger(StickClientListener.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from predecessor: IOException" + e);
                System.exit(1);
            }
        }

        // Exit 0 not return, so we also terminate the other threads
        System.exit(0);
    }

    private boolean read(Socket serverSocket) throws IOException
    {
        StickMessage incomingMessage = null;
        ObjectInputStream fromOther = new ObjectInputStream(serverSocket.getInputStream());

        // Read the message the server sent
        try
        {
            incomingMessage = (StickMessage) fromOther.readObject();
        }
        catch (ClassNotFoundException e)
        {
            Logger.getLogger(StickClientListener.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error receiving message: ClassNotFoundException" + e);
            System.exit(1);
        }

        // Report the note from the incoming message
        System.out.println(incomingMessage.origin.name + ": " + incomingMessage.note);
        // Handle the message appropriately
        handle(incomingMessage);

        // If someone sent a shutdown all the message propagated will be of type SHUTDOWN_ALL, and we want to exit
        return incomingMessage.type != StickMessageTypes.SHUTDOWN_ALL;
    }

    private void handle(StickMessage incomingMessage)
    {
        StickMessage toSend = null;

        switch (incomingMessage.type)
        {
            case JOIN:
                // Deal with adding the node to the chat
                handleJoin(incomingMessage);
                break;
            case NOTE:
            case LEAVE:
            case SHUTDOWN:
            case SHUTDOWN_ALL:
                toSend = incomingMessage;
                break;
            case UPDATE_PRED:
                // If this happens, we are the end of the stick, so we want to be pointing at ourselves
                if (predecessorInfo.equals(incomingMessage.other))
                {
                    predecessorInfo.syncWrite(myInfo);
                }
                // Ensure we don't have the same node as both predecessor and successor
                else if (!successorInfo.equals(incomingMessage.other))
                {
                    predecessorInfo.syncWrite(incomingMessage.other);
                }

                break;
            case UPDATE_SUCC:
                // If this happens, we are the end of the stick, so we want to be pointing at ourselves
                if (successorInfo.equals((incomingMessage.other)))
                {
                    successorInfo.syncWrite(myInfo);
                }
                // Ensure we don't have the same node as both predecessor and successor
                else if (!predecessorInfo.equals(incomingMessage.other))
                {
                    successorInfo.syncWrite(incomingMessage.other);
                }
        }

        // Don't send the message to yourself if you are at the end of the stick in that direction
        if (toSend != null)
        {
            sender.send(toSend, myInfo, predecessorInfo, successorInfo);
        }

    }

    // Join is a sort of special case message that requires special handling
    private void handleJoin(StickMessage incomingMessage)
    {
        // Create message to send to joining node
        StickMessage toSend;
        StickNodeInfo oldSuccessorInfo = new StickNodeInfo(successorInfo);

        // Tell our current successor to change their predecessor
        toSend = new StickMessage(myInfo, incomingMessage.origin,
                          "A new node has joined, sending your new predecessor info.", UPDATE_PRED, SUCCESSOR);
        sender.send(toSend, myInfo, predecessorInfo, successorInfo);

        // Update your successor to be the joining node
        successorInfo.syncWrite(incomingMessage.origin);

        // Tell the joining node to update its info
        toSend = new StickMessage(myInfo, oldSuccessorInfo,
                          "Sending your new successor info", UPDATE_SUCC, SUCCESSOR);

        // Send updated info to the joining node
        sender.send(toSend, myInfo, predecessorInfo, successorInfo);

        // Send out the announcement that a new node has joined
        sender.send(new StickMessage(myInfo, successorInfo, incomingMessage.note, NOTE, PREDECESSOR), myInfo,
                    predecessorInfo, successorInfo);
        sender.send(new StickMessage(myInfo, successorInfo, incomingMessage.note, NOTE, SUCCESSOR), myInfo,
                    predecessorInfo, successorInfo);
    }
}
