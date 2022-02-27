package Client;

import Utils.*;

import java.util.Locale;
import java.util.Scanner;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StickUserListener extends Thread implements StickMessageTypes, StickDirections
{
    private final StickNodeInfo myInfo;
    private final StickNodeInfo predecessorInfo;
    private final StickNodeInfo successorInfo;
    private final StickSender sender;

    public StickUserListener(StickNodeInfo initMyInfo, StickNodeInfo initPredecessorInfo, StickNodeInfo initSuccessorInfo)
    {
        myInfo = initMyInfo;
        predecessorInfo = initPredecessorInfo;
        successorInfo = initSuccessorInfo;
        sender = new StickSender();
    }

    public void run()
    {
        boolean inChat = false;
        boolean isRunning = true;

        Scanner scanner = new Scanner(System.in);
        String input;
        StickMessage toSend;

        while (isRunning)
        {
            // Parse out a message from what they typed
            input = scanner.nextLine();
            toSend = parse(input);

            // This should only happen if we just started a new chat
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

                // If we are sending to both, send it to the predecessor then the successor
                if (toSend.direction == BOTH)
                {
                    toSend.direction = PREDECESSOR;
                    sender.send(toSend, myInfo, predecessorInfo, successorInfo);
                    toSend.direction = SUCCESSOR;
                }

                sender.send(toSend, myInfo, predecessorInfo, successorInfo);

                if (toSend.type == LEAVE || toSend.type == SHUTDOWN)
                {
                    // Update surrounding nodes so the chat can continue after we leave
                    sender.send(new StickMessage(myInfo, successorInfo, "Sending your new successor",
                                                 UPDATE_SUCC, PREDECESSOR), myInfo, predecessorInfo, successorInfo);
                    sender.send(new StickMessage(myInfo, predecessorInfo, "Sending your new predecessor",
                                                 UPDATE_PRED, SUCCESSOR), myInfo, predecessorInfo, successorInfo);

                    System.out.println("Goodbye " + myInfo.name + ".");

                    // Reset our predecessor and successor so if we are leaving we rejoin with a fresh start
                    predecessorInfo.syncWrite(myInfo);
                    successorInfo.syncWrite(myInfo);

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

    private StickMessage parse(String input)
    {
        // Split our string in case we have a join followed by args
        String[] splitInput = input.split(" ");
        String messageType;

        // Message we will be sending
        StickMessage newMessage = null;
        StickNodeInfo target;

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
                    target = new StickNodeInfo("Target", splitInput[1], Integer.parseInt(splitInput[2]));
                    // Set our predecessor
                    predecessorInfo.syncWrite(target);
                    newMessage = new StickMessage(myInfo, target, myInfo.name + " has joined the chat.", JOIN,
                                                  PREDECESSOR);
                }
            }
            case "LEAVE" -> newMessage = new StickMessage(myInfo, successorInfo,
                                                  myInfo.name + " is leaving the chat.", LEAVE, BOTH);
            case "SHUTDOWN" -> newMessage = new StickMessage(myInfo, successorInfo,
                                                     myInfo.name + " is shutting down.", SHUTDOWN, BOTH);
            case "SHUTDOWN_ALL" -> newMessage = new StickMessage(myInfo, successorInfo,
                                                         myInfo.name + " initiated shutdown all.",
                                                                 SHUTDOWN_ALL, BOTH);
            default -> newMessage = new StickMessage(myInfo, successorInfo, input, NOTE, BOTH);
        }

        return newMessage;
    }
}
