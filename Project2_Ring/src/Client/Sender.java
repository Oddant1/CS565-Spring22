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
    private NodeInfo successorInfo;

    public Sender(NodeInfo initMyInfo, NodeInfo initSuccessorInfo)
    {
        myInfo = initMyInfo;
        successorInfo = initSuccessorInfo;
    }

    public void run()
    {
        Socket socket;

        boolean inChat = false;
        boolean isRunning = true;

        Scanner scanner = new Scanner(System.in);
        String input;
        Message toSend;

        while (isRunning)
        {
            input = scanner.nextLine();
            toSend = parse(input);

            // Some kinda parse error
            if (toSend == null)
            {
                continue;
            }

            if (toSend.type == JOIN)
            {
                if (inChat)
                {
                    System.out.println("You have already joined the chat.");
                }
                else
                {
                    System.out.println("Welcome to the chat " + myInfo.name + ".");
                    inChat = true;
                }
            }
            else if (!inChat)
            {
                System.out.println("Please join a chat before attempting to send a message");
                continue;
            }

            if (!toSend.origin.ip.equals(toSend.successor.ip) || toSend.origin.port != toSend.successor.port)
            {
                try
                {
                    socket = new Socket(successorInfo.ip, successorInfo.port);
                    send(toSend, socket);
                } catch (IOException e)
                {
                    Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, e);
                    System.err.println("Failed to send message:\n" + e);
                    System.exit(1);
                }
            }

            if (toSend.type == LEAVE)
            {
                isRunning = false;
                inChat = false;
            }

            if (toSend.type == SHUTDOWN || toSend.type == SHUTDOWN_ALL)
            {
                // Exit here to close entire process
                System.exit(0);
            }
        }
    }

    private Message parse(String input)
    {
        // Split our string in case we have a join followed by args
        String[] splitInput = input.split(" ");

        // Message we will be sending
        Message newMessage = null;

        switch (splitInput[0].toUpperCase(Locale.ROOT))
        {
            case "JOIN":
                if (splitInput.length == 1)
                {
                    // If we are creating a new chat, we are effectively joining ourselves
                    newMessage = new Message(myInfo, myInfo, myInfo.name + " has joined the chat.", JOIN);
                }
                else if (splitInput.length == 3)
                {
                    // Name here does not matter, only connection info
                    successorInfo.ip = splitInput[1];
                    successorInfo.port = Integer.parseInt(splitInput[2]);

                    newMessage = new Message(myInfo, successorInfo, myInfo.name + " has joined the chat.", JOIN);
                }
                else
                {
                    System.out.println(
                            "Please enter \"JOIN\" followed by no arguments or \"JOIN\" followed only by target IP " +
                            "and port.");
                }
        }

        return newMessage;
    }

    private void send(Message toSend, Socket socket) throws IOException
    {
        ObjectOutputStream toSuccessor = new ObjectOutputStream(socket.getOutputStream());
        toSuccessor.writeObject(toSend);
    }
}
