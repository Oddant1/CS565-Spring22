package Client;

import Utils.*;

import java.util.Scanner;

import java.io.*;

public class RingClient
{
    private final static String DEFAULT_PROPERTIES_PATH = "properties.txt";

    private final RingUserListener mySender;
    private final RingClientListener myReceiver;

    public static void main(String[] args)
    {
        File properties = null;
        RingClient client;

        // Determine where to look for our properties
        if (args.length == 0)
        {
            properties = new File(DEFAULT_PROPERTIES_PATH);
        }
        else if (args.length == 1)
        {
            properties = new File(args[0]);
        }
        // If they provided more than 1 command line arg we error
        else
        {
            System.out.println("The only command line argument should (optionally) be a path to a properties file");
            System.exit(1);
        }

        // Create our new client and start it running
        client = new RingClient(properties);
        client.run();
    }

    private RingClient(File properties)
    {
        // These objects are shared with the Sender and Receiver threads
        final RingNodeInfo myInfo;
        final RingNodeInfo successorInfo;

        Scanner scanner = null;

        String name;
        String ip;
        int port;

        // Make sure our properties file exists
        try
        {
            scanner = new Scanner(properties);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Failed to find properties file.");
            System.exit(1);
        }

        // Get our IP and port
        ip = scanner.nextLine();
        port = Integer.parseInt(scanner.nextLine());
        scanner.close();

        scanner = new Scanner(System.in);

        // Get our name
        System.out.println("Please enter your name: ");
        name = scanner.nextLine();
        System.out.println(
                "Hello " + name + ". Please type \"JOIN\" to enter the chat. Please also type the ip and port of " +
                "the peer you are joining if trying to join an existing chat");

        // Create the NodeInfo for this client and point our successor at ourselves
        myInfo = new RingNodeInfo(name, ip, port);
        successorInfo = new RingNodeInfo(myInfo);

        // Create the sender and receiver for this client
        mySender = new RingUserListener(myInfo, successorInfo);
        myReceiver = new RingClientListener(myInfo, successorInfo);
    }

    private void run()
    {
        mySender.start();
        myReceiver.start();
    }
}
