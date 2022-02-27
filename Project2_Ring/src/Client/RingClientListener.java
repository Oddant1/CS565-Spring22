package Client;

import Utils.*;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RingClientListener extends Thread implements RingMessageTypes
{
    private final RingNodeInfo myInfo;
    private final RingNodeInfo successorInfo;

    public RingClientListener(RingNodeInfo initMyInfo, RingNodeInfo initSuccessorInfo)
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
            Logger.getLogger(RingClientListener.class.getName()).log(Level.SEVERE, null, e);
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
                Logger.getLogger(RingClientListener.class.getName()).log(Level.SEVERE, null, e);
                System.err.println("Error receiving message from predecessor: IOException" + e);
                System.exit(1);
            }
        }

        // Exit 0 not return, so we also terminate the other threads
        System.exit(0);
    }

    private boolean read(Socket serverSocket) throws IOException
    {
        RingMessage incomingMessage = null;
        ObjectInputStream fromOther = new ObjectInputStream(serverSocket.getInputStream());

        // Read the message the server sent
        try
        {
            incomingMessage = (RingMessage) fromOther.readObject();
        }
        catch (ClassNotFoundException e)
        {
            Logger.getLogger(RingClientListener.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error receiving message: ClassNotFoundException" + e);
            System.exit(1);
        }

        // Report the note from the incoming message
        System.out.println(incomingMessage.origin.name + ": " + incomingMessage.note);
        // Handle the message appropriately
        handle(incomingMessage);

        // If someone sent a shutdown all the message propagated will be of type SHUTDOWN_ALL, and we want to exit
        return incomingMessage.type != RingMessageTypes.SHUTDOWN_ALL;
    }

    private void handle(RingMessage incomingMessage)
    {
        RingMessage toSend = null;

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
                // If we have gotten all the way around the ring then close it (superfluous for shutdown_all,
                // I just wanted to keep the cases in order if possible, and it was easy enough to do here)
                if (incomingMessage.origin.equals(successorInfo))
                {
                    successorInfo.syncWrite(incomingMessage.other);
                }
                else
                {
                    toSend = incomingMessage;
                }

                break;
            case UPDATE:
                successorInfo.syncWrite(incomingMessage.other);
        }

        // If my successor isn't the origin of the message, and if we have a message to send, forward the message
        if (!successorInfo.equals(incomingMessage.origin) && toSend != null)
        {
            send(toSend);
        }
    }

    // Join is a sort of special case message that requires special handling
    private void handleJoin(RingMessage incomingMessage)
    {
        // Create message to send to joining node
        RingMessage toSend;
        RingNodeInfo oldSuccessorInfo = new RingNodeInfo(successorInfo);

        // Update your successor to be the joining node
        successorInfo.syncWrite(incomingMessage.origin);

        // Tell the joining node to update its info
        toSend = new RingMessage(myInfo, oldSuccessorInfo, "Sending your new successor info", UPDATE);

        // Send updated info to the joining node
        send(toSend);
        send(new RingMessage(myInfo, successorInfo, incomingMessage.note, NOTE));
    }

    private void send(RingMessage toSend)
    {
        try
        {
            Socket socket = new Socket(successorInfo.syncReadIP(), successorInfo.syncReadPort());
            ObjectOutputStream toSuccessor = new ObjectOutputStream(socket.getOutputStream());
            toSuccessor.writeObject(toSend);
        }
        catch (IOException e)
        {
            Logger.getLogger(RingClientListener.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error sending message: IOException " + e);
            System.exit(1);
        }
    }
}
