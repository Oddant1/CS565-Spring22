/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package client;

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
                                   int initAmount, String IP, int port)
    {
        
    }
    
    @Override
    public void run()
    {
        // use proxy to
        // open transaction
        // read source
        // write source - amount
        // read destination
        // write desination + amount
        // close transaction
    }
}
