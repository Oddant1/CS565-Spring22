/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import util.*;
import message.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * Generates X transactions and sets them running in their own threads
 * 
 * @author anthony
 */
public class TransactionClient implements MessageTypes
{
    private final static String DEFAULT_PROPERTIES_PATH = "client_properties.txt";
    
    private Scanner scanner = null;
    private Random random = new Random();
    
    private final ArrayList<TransactionClientWorker> workers;
    
    private final String myIp;
    
    private final String serverIp;
    private final int serverPort;

    private final int numAccounts;
    private final int maxTransfer;
    private final int numTransactions;

    // We are going to want to receive a path to a file containing server
    // connection info, num accounts, and num transactions
    public static void main(String[] args)
    {
        TransactionClient client;
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
        
        client = new TransactionClient(properties);
        client.runClient();
    }
     
    private TransactionClient(File properties)
    {   
        workers = new ArrayList();
        
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
        myIp = scanner.nextLine();

        serverIp = scanner.nextLine();
        serverPort = Integer.parseInt(scanner.nextLine());
        
        numAccounts = Integer.parseInt(scanner.nextLine());
        maxTransfer = Integer.parseInt(scanner.nextLine());
        numTransactions = Integer.parseInt(scanner.nextLine());
    }
    
    
    // Create our transactions
    private void runClient()
    {
        SenderReceiver senderReceiver = new SenderReceiver(null, serverIp, serverPort);
        Message toSend = new Message(DEFAULT, SHUTDOWN, DEFAULT, DEFAULT, "DEFAULT", DEFAULT);
        
        // Create all our transaction
        for (int i = 0; i < numTransactions; i++)
        {
            createTransaction();
        }

        // Wait for all of our threads to exit
        for (TransactionClientWorker transaction : workers)
        {
            try
            {
                transaction.join();
            }
            catch (InterruptedException e)
            {
                Logger.getLogger(TransactionClient.class.getName()).log(Level.SEVERE, null, e);
                System.out.println("Transaction interrupted:\n" + e);
                System.exit(1);   
            }
        }
        
        // Send a shutdown to the server
        senderReceiver.send(toSend);
    }
    
    // Create a single transaction that has source 0 - (numAccounts - 1)
    // destination 0 - (numAccounts - 1) and amount 1 - 10
    public void createTransaction()
    {
        final TransactionClientWorker newTransaction;
        
        final int source = random.nextInt(numAccounts);
        final int destination = random.nextInt(numAccounts);
        final int amount = random.nextInt(maxTransfer);
               
        newTransaction = new TransactionClientWorker(source, destination, amount, serverIp, serverPort, myIp);
        workers.add(newTransaction);
        newTransaction.start();
    }
}
