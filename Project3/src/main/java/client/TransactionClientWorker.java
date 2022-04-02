/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import aborted.*;

import java.io.*;
import java.util.logging.*;

/**
 * Handles the transactions in parallel for the client
 * 
 * @author anthony
 */
public class TransactionClientWorker extends Thread
{
    private final int source;
    private final int destination;
    private final int amount;
    
    private final TransactionServerProxy myProxy;
    
    
    // Init object and create proxy for communication
    public TransactionClientWorker(int initSource, int initDestination,
                                   int initAmount, String initServerIp, 
                                   int initServerPort, String initMyIp)
    {
        source = initSource;
        destination = initDestination;
        amount = initAmount;
        
        myProxy = new TransactionServerProxy(initServerIp, initServerPort,
                                             initMyIp);
    }
    
    @Override
    public void run()
    {
        boolean succeeded = false;
        
        // Restart the transaction if we abort until we succeed
        while (!succeeded)
        {
            try
            {
                succeeded = executeTransaction();
            }
            catch (AbortedException e)
            {
                // TODO: Some kinda report about aborting here
                myProxy.resetRemoteInfo();
            }
        }
    }
    
    // Execute the commands of this transaction
    public boolean executeTransaction() throws AbortedException
    {
        int sourceAmount;
        int destinationAmount;
        
        // use proxy to
        // open transaction
        myProxy.openTransaction();
        
        // read source
        sourceAmount = myProxy.read(source);
        System.out.println("READ SOURCE: " + source + " " + sourceAmount);
        // write source - amount
        myProxy.write(source, sourceAmount - amount);
        System.out.println("WRITE SOURCE: " + source + " " + (sourceAmount - amount));
        
        // read destination
        destinationAmount = myProxy.read(destination);
        System.out.println("READ DESTINATION: " + destination + " " + destinationAmount);
        // write desination + amount
        myProxy.write(destination, destinationAmount + amount);
        System.out.println("WRITE DESTINATION: " + destination + " " + (destinationAmount + amount));
        
        // close transaction
        myProxy.closeTransaction();
        
        // Need to close this or we have a potential port leak
        // We need to do it here so we know we have committed our transaction
        try
        {
            myProxy.serverSocket.close();
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionClientWorker.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed to close server socket on port: " + myProxy.myPort + ".");
            System.exit(1);
        } 
        
        return true;
    }
}
