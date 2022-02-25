package Client;

import Utils.*;

import java.util.Locale;
import java.util.Scanner;

import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender extends Thread implements MessageTypes
{
    private final NodeInfo myInfo;
    private final NodeInfo successorInfo;

    boolean inChat = false;

    public Sender(NodeInfo initMyInfo, NodeInfo initSuccessorInfo)
    {
        myInfo = initMyInfo;
        successorInfo = initSuccessorInfo;
    }

    public void run()
    {
        boolean isRunning = true;

        Scanner scanner = new Scanner(System.in);
        String input;
        Message toSend;

        while (isRunning)
        {
            input = scanner.nextLine();
            toSend = parse(input);

            if ((toSend.type == JOIN && !inChat) || (toSend.type != JOIN && inChat))
            {
                if (toSend.type == JOIN)
                {
                    if (toSend.other.equals(myInfo))
                    {
                        System.out.println("You have started a new chat. Peers can join you at IP: " + myInfo.ip + " Port: " + myInfo.port);
                    }
                    else
                    {
                        System.out.println("Welcome to the chat " + myInfo.name + ".");
                    }

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
        Message newMessage;
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
                    // Temporarily set our successor to the target, it will send us new successor info
                    successorInfo.syncWrite(target);
                    newMessage = new Message(myInfo, target, myInfo.name + " has joined the chat.", JOIN);
                }
                else
                {
                    // If we are creating a new chat, we are effectively joining ourselves
                    newMessage = new Message(myInfo, myInfo, myInfo.name + " has joined the chat.", JOIN);
                }
            }
            case "LEAVE" -> newMessage = new Message(myInfo, successorInfo, myInfo.name + " is leaving the chat.", LEAVE);
            case "SHUTDOWN" -> newMessage = new Message(myInfo, successorInfo, myInfo.name + " is shutting down.", SHUTDOWN);
            case "SHUTDOWN_ALL" -> newMessage = new Message(myInfo, successorInfo, myInfo.name + " initiated shutdown all.", SHUTDOWN_ALL);
            default -> newMessage = new Message(myInfo, successorInfo, input, NOTE);
        }

        return newMessage;
    }

    private void send(Message toSend)
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
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Failed to send message:\n" + e);
            System.exit(1);
        }

    }
}