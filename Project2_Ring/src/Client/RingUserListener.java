package Client;

import Utils.*;

import java.util.Locale;
import java.util.Scanner;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RingUserListener extends Thread implements RingMessageTypes
{
    private final RingNodeInfo myInfo;
    private final RingNodeInfo successorInfo;

    public RingUserListener(RingNodeInfo initMyInfo, RingNodeInfo initSuccessorInfo)
    {
        myInfo = initMyInfo;
        successorInfo = initSuccessorInfo;
    }

    public void run()
    {
        boolean inChat = false;
        boolean isRunning = true;

        Scanner scanner = new Scanner(System.in);
        String input;
        RingMessage toSend;

        while (isRunning)
        {
            // Parse out a message from what they typed
            input = scanner.nextLine();
            toSend = parse(input);

            // We only get a null if we started a new chat
            if (toSend == null && !inChat)
            {
                System.out.println("You have started a new chat. Peers can join you at IP: "
                                   + myInfo.ip + " Port: " + myInfo.port);
                inChat = true;
            }
            // Determine if we are in/joining a chat
            else if (toSend != null && ((toSend.type == JOIN && !inChat) || (toSend.type != JOIN && inChat)))
            {
                if (toSend.type == JOIN)
                {
                    System.out.println("Welcome to the chat " + myInfo.name + ".");
                    inChat = true;
                }

                // Don't bother sending things to ourselves if we're alone in the chat
                if (!successorInfo.equals(myInfo))
                {
                    send(toSend);
                }

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
            // Tried to send a message without being in a chat
            else if (!inChat)
            {
                System.out.println("Please join a chat before attempting to send a message");
            }
            // Tried to join a chat while already in a chat
            else
            {
                System.out.println("You are already in a chat, please leave before attempting to join another");
            }
        }

        // Exit here to close entire process
        System.out.println("Goodbye " + myInfo.name);
        System.exit(0);
    }

    private RingMessage parse(String input)
    {
        // Split our string in case we have a join followed by args
        String[] splitInput = input.split(" ");
        String messageType;

        // Message we will be sending
        RingMessage newMessage = null;
        RingNodeInfo target;

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
                    target = new RingNodeInfo("Target", splitInput[1], Integer.parseInt(splitInput[2]));
                    // Temporarily set our successor to the target, it will send us new successor info
                    successorInfo.syncWrite(target);
                    newMessage = new RingMessage(myInfo, target, myInfo.name + " has joined the chat.", JOIN);
                }
            }
            case "LEAVE" -> newMessage = new RingMessage(myInfo, successorInfo,
                                                 myInfo.name + " is leaving the chat.", LEAVE);
            case "SHUTDOWN" -> newMessage = new RingMessage(myInfo, successorInfo,
                                                    myInfo.name + " is shutting down.", SHUTDOWN);
            case "SHUTDOWN_ALL" -> newMessage = new RingMessage(myInfo, successorInfo,
                                                        myInfo.name + " initiated shutdown all.", SHUTDOWN_ALL);
            default -> newMessage = new RingMessage(myInfo, successorInfo, input, NOTE);
        }

        return newMessage;
    }

    private void send(RingMessage toSend)
    {
        Socket socket;
        ObjectOutputStream toSuccessor;

        try
        {
            // If we are joining we are sending to the info entered by the user
            if (toSend.type == JOIN)
            {
                socket = new Socket(toSend.other.ip, toSend.other.port);
            }
            // Otherwise, send to our successor
            else
            {
                socket = new Socket(successorInfo.syncReadIP(), successorInfo.syncReadPort());
            }

            toSuccessor = new ObjectOutputStream(socket.getOutputStream());
            toSuccessor.writeObject(toSend);
        }
        catch (IOException e)
        {
            Logger.getLogger(RingUserListener.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Failed to send message:\n" + e);
            System.exit(1);
        }
    }
}
