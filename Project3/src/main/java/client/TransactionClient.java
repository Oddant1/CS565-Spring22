/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * Generates X transactions and sets them running in their own threads
 * 
 * @author anthony
 */
public class TransactionClient 
{
    private final static String DEFAULT_PROPERTIES_PATH = "properties.txt";
    
    // We are going to want to receive a path to a file containing server
    // connection info, num accounts, and num transactions
    public static void main(String[] args)
    {
        TransactionClient client = null;
        File properties = null;

        // Determine where to look for our properties
        switch (args.length)
        {
            case 0:
                properties = new File(DEFAULT_PROPERTIES_PATH);
                break;
            case 1:
                properties = new File(args[0]);
                break;
            // If they provided more than 1 command line arg we error
            default:
                System.out.println("The only command line argument should (optionally) be a path to a properties file.");
                System.exit(1);
        }

        // Create our new client and start it running
        client = new TransactionClient(properties);
        client.run();
    }
    
    // Init the server and the managers
    private TransactionClient(File properties)
    {
        final String ip;
        
        final int port;
        final int numAccounts;
        final int accountBalances;
        
        Scanner scanner = null;
       
        // Get our properties scanner
        try
        {
            scanner = new Scanner(properties);
        }
        catch (FileNotFoundException e)
        {
            Logger.getLogger(TransactionClient.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed to find properties file.");
            System.exit(1);
        }
        
        // Get our properties
        ip = scanner.nextLine();
        port = Integer.parseInt(scanner.nextLine());
        numAccounts = Integer.parseInt(scanner.nextLine());
        accountBalances = Integer.parseInt(scanner.nextLine());
               
        // Get our socket
        try
        {
            serverSocket = new ServerSocket(port);
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionServer.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed to open server socket on port " + port + ".");
            System.exit(1);
        }
    }
        
    // Spawn workers that handle the transactions in parallel
    public void run()
    {
        
    }
    
    // Create a single transaction that has source 0 - (numAccounts - 1)
    // destination 0 - (numAccounts - 1) and amount 1 - 10
    public void createTransaction()
    {
        
    }
}
