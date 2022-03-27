/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.transaction;

import server.account.AccountManager;
import java.util.*;
import java.util.logging.*;
import server.lock.LockManager;

import java.io.*;
import java.net.*;

/**
 * Creates the transaction, lock, and account managers then pawns work off onto
 * them
 *
 * @author anthony
 */
public class TransactionServer 
{
    private final static String DEFAULT_PROPERTIES_PATH = "server_properties.txt";

    private final AccountManager accountManager;
    private final LockManager lockManager;
    private final TransactionManager transactionManager;
    
    private ServerSocket serverSocket;
    
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
            Logger.getLogger(TransactionServer.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed to find properties file.");
            System.exit(1);
        }
        
        // Get our properties
        ip = scanner.nextLine();
        port = Integer.parseInt(scanner.nextLine());
        numAccounts = Integer.parseInt(scanner.nextLine());
        accountBalances = Integer.parseInt(scanner.nextLine());
        
        // Get our managers
        accountManager = new AccountManager(numAccounts, accountBalances);
        lockManager = new LockManager(numAccounts);
        transactionManager = new TransactionManager();
        
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
    
    // Await requests for new transactions and hand them off to the transaction
    // mananger. Maybe we eliminate this and just have the transaction manager
    // recevie the requsts? Then all this class does is kickstart things
    public void run()
    {
        while (true)
        {
            
        }
    }
}
