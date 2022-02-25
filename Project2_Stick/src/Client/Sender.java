package Client;

import Utils.*;

import java.util.Locale;
import java.util.Scanner;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender extends Thread implements MessageTypes, Directions
{
    private final NodeInfo myInfo;
    private final NodeInfo predecessorInfo;
    private final NodeInfo successorInfo;

    public Sender(NodeInfo initMyInfo, NodeInfo initPredecessorInfo, NodeInfo initSuccessorInfo)
    {
        myInfo = initMyInfo;
        predecessorInfo = initPredecessorInfo;
        successorInfo = initSuccessorInfo;
    }

    public void run()
    {
        boolean inChat = false;
        boolean isRunning = true;

        Scanner scanner = new Scanner(System.in);
        String input;
        Message toSend;

        while (isRunning)
        {
            input = scanner.nextLine();
            toSend = parse(input);

            // This should only happen if we just started a new chat
            if (toSend == null && !inChat)
            {
                System.out.println("You have started a new chat. Peers can join you at IP: " + myInfo.ip + " Port: " + myInfo.port);
                inChat = true;
            }
            else if (toSend != null && ((toSend.type == JOIN && !inChat) || (toSend.type != JOIN && inChat)))
            {
                if (toSend.type == JOIN)
                {
                    System.out.println("Welcome to the chat " + myInfo.name + ".");
                    inChat = true;
                }

                if (toSend.direction == BOTH)
                {
                    toSend.direction = PREDECESSOR;
                    send(toSend);
                    toSend.direction = SUCCESSOR;
                }

                send(toSend);

                if (toSend.type == LEAVE)
                {
                    System.out.println("Goodbye " + myInfo.name + ", you may rejoin an existing chat or start your own");
                    inChat = false;
                }

                if (toSend.type == SHUTDOWN || toSend.type == SHUTDOWN_ALL)
                {
                    isRunning = false;
                }
            }
            else if (!inChat)
            {
                System.out.println("Please join a chat before attempting to send a message");
            }
            else
            {
                System.out.println("You are already in a chat, please leave before attempting to join another");
            }
        }

        // Exit here to close entire process
        System.out.println("Goodbye " + myInfo.name);
        System.exit(0);
    }

    private Message parse(String input)
    {
        // Split our string in case we have a join followed by args
        String[] splitInput = input.split(" ");
        String messageType;

        // Message we will be sending
        Message newMessage = null;
        NodeInfo target;

        // Handle the user input being only whitespace. If it is, we want to send that whitespace as a note
        if (splitInput.length > 0)
        {
            messageType = splitInput[0];
        }
        else
        {
            messageType = input;
        }

        switch (messageType.toUpperCase(Locale.ROOT))
        {
            case "JOIN" ->
            {
                if (splitInput.length == 3)
                {
                    target = new NodeInfo("Target", splitInput[1], Integer.parseInt(splitInput[2]));
                    // Set our predecessor
                    predecessorInfo.syncWrite(target);
                    newMessage = new Message(myInfo, target, myInfo.name + " has joined the chat.", JOIN, PREDECESSOR);
                }
            }
//            case "LEAVE" -> newMessage = new Message(myInfo, successorInfo, myInfo.name + " is leaving the chat.", LEAVE);
//            case "SHUTDOWN" -> newMessage = new Message(myInfo, successorInfo, myInfo.name + " is shutting down.", SHUTDOWN);
//            case "SHUTDOWN_ALL" -> newMessage = new Message(myInfo, successorInfo, myInfo.name + " initiated shutdown all.", SHUTDOWN_ALL);
            default -> newMessage = new Message(myInfo, successorInfo, input, NOTE, BOTH);
        }

        return newMessage;
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
