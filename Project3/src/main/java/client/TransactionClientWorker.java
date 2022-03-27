/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import aborted.*;

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
                                   int initServerPort, String initMyIp,
                                   int initMyPort)
    {
        source = initSource;
        destination = initDestination;
        amount = initAmount;
        
        myProxy = new TransactionServerProxy(initServerIp, initServerPort,
                                             initMyIp, initMyPort);
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
        // write source - amount
        myProxy.write(source, sourceAmount - amount);
        
        // read destination
        destinationAmount = myProxy.read(destination);
        // write desination + amount
        myProxy.write(destination, destinationAmount + amount);
        
        // close transaction
        myProxy.closeTransaction();
        
        return true;
    }
}
