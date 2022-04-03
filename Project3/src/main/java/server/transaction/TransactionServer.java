/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.transaction;

import util.*;
import message.*;
import server.lock.LockManager;
import server.account.AccountManager;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * Creates the transaction, lock, and account managers then pawns work off onto
 * them
 *
 * @author anthony
 */
public class TransactionServer implements MessageTypes
{
    private final static String DEFAULT_PROPERTIES_PATH = "server_properties.txt";

    private final AccountManager accountManager;
    private final LockManager lockManager;
    private final TransactionManager transactionManager;
    
    private ServerSocket serverSocket;
    private final SenderReceiver senderReceiver;
    
    private final String ip;
    private final int port;
    
    // We are going to want to receive a file telling us which port to open, num
    // accounts and initial balances then create all managers
    public static void main(String[] args)
    {
        TransactionServer server = null;
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
        server = new TransactionServer(properties);
        server.run();
    }
    
    // Init the server and the managers
    private TransactionServer(File properties)
    {
        final int numAccounts;
        final int accountBalances;
        final boolean locking;
        
        Scanner scanner = null;
       
        // Get our properties scanner
        try
        {
            scanner = new Scanner(properties);
        }
        catch (FileNotFoundException e)
        {
            Logger.getLogger(TransactionServer.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed to find properties file.");
            System.exit(1);
        }
        
        // Get our properties
        ip = scanner.nextLine();
        port = Integer.parseInt(scanner.nextLine());
        numAccounts = Integer.parseInt(scanner.nextLine());
        accountBalances = Integer.parseInt(scanner.nextLine());
        locking = Boolean.parseBoolean(scanner.nextLine());
        
        scanner.close();
        
        // Get our managers
        lockManager = new LockManager(numAccounts);
        System.out.println("LockManager created");
        
        accountManager = new AccountManager(numAccounts, accountBalances,
                                            lockManager, locking);
        System.out.println("AccountManager created");
        
        transactionManager = new TransactionManager(accountManager, lockManager, ip);
        System.out.println("TransactionManager created");
        
        // Get our socket
        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println("ServerSocket created");
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionServer.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed to open server socket on port " + port + ".");
            System.exit(1);
        }
        
        // Create our io manager
        senderReceiver = new SenderReceiver(serverSocket, ip, port);
    }
    
    // Await requests for new transactions and hand them off to the transaction
    // mananger. Also handles shutdown request
    public void run()
    {
        boolean isRunning = true;
        Message received;
        
        System.out.println("\n======================================= PRE BRANCH TOTAL =======================================\n"
                + "---> " + accountManager.sumAccounts());
        
        // The server runs until it receives a SHUTDOWN from the client
        while (isRunning)
        {
            received = senderReceiver.receive();
            
            switch (received.type)
            {
                case OPEN:
                    transactionManager.createTransaction(received);
                    break;
                case SHUTDOWN:
                    isRunning = false;
                    break;
                default:
                    System.out.println("Received invalid message type for server: " + received.type);
                    System.exit(1);
            }
        }
        
        System.out.println("\n======================================= BEGIN COMMITTED TRANSACTIONS INFORMATION =======================================\n");
        
        for (String committed : transactionManager.committedLog)
        {
            System.out.println(committed);
        }
        
        System.out.println("======================================= END COMMITTED TRANSACTIONS INFORMATION =======================================\n"
                         + "\n======================================= BEGIN ABORTED TRANSACTIONS INFORMATION =======================================\n");
        
        for (String aborted : transactionManager.abortedLog)
        {
            System.out.println(aborted);
        }
        
        System.out.println("======================================= END ABORTED TRANSACTIONS INFORMATION =======================================\n"
                         + "\n======================================= END BRANCH TOTAL =======================================\n"
                         + "---> " + accountManager.sumAccounts() + "\n"
                         + "======================================= ACCOUNT BALANCES =======================================");
        
        for (int accountBalance : accountManager.accounts)
        {
            System.out.print(accountBalance + " ");
        }
    }
}
