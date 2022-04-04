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
                System.out.println(e.getMessage());
                myProxy.resetRemoteInfo();
            }
        }
        
        System.out.println("Transaction #" + myProxy.tid + " COMMITTED");
    }
    
    // Execute the commands of this transaction
    public boolean executeTransaction() throws AbortedException
    {
        String outBuffer;
        
        int sourceAmount;
        int destinationAmount;
        
        // use proxy to
        // open transaction
        myProxy.openTransaction();
        
        // If this is the first time opening the transaction
        if (myProxy.oldTid == -1)
        {
            outBuffer = "Transaction #" + myProxy.tid + " started";
        }
        // If we are reopening the transaction
        else
        {
            outBuffer = 
                    "                Prior transaction #" + myProxy.oldTid + 
                    " restarted as transaction #" + myProxy.tid;
        }
        
        System.out.println(outBuffer + ", transfer $" + amount + ": " + source
                           + "->" + destination);
        
        // START WITHDRAWAL
        // read source
        sourceAmount = myProxy.read(source);
        // write source - amount
        myProxy.write(source, sourceAmount - amount);
        // END WITHDRAWAL
        
        // START DEPOSIT
        // read destination
        destinationAmount = myProxy.read(destination);
        // write desination + amount
        myProxy.write(destination, destinationAmount + amount);
        // END DEPOSIT
        
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
            Logger.getLogger(TransactionClientWorker.class.getName())
                    .log(Level.SEVERE, null, e);
            System.out.println("Failed to close server socket on port: " 
                               + myProxy.myPort + ".");
            System.exit(1);
        } 
        
        return true;
    }
}
