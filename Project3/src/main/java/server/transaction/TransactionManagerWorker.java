/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.transaction;

import message.Message;
import message.MessageTypes;

import java.util.ArrayList;

/**
 * This basically represents a single transaction on the server
 * 
 * @author anthony
 */
public class TransactionManagerWorker extends Thread implements MessageTypes
{
    // This will likely also need a reference to the account and lock managers
    // All locks currently held by the transaction
    public ArrayList<Integer> heldLocks;
    
    public final int tid;
    
    private final String myIp;
    private final int myPort;
    
    private final String clientIp;
    private final int clientPort;
    
    public TransactionManagerWorker(int initTid, String initMyIp,
                                    int initMyPort, String initClientIp,
                                    int initClientPort)
    {
         
    }
    
    // Receive requests from our client
    public void run()
    {
        
    }
    
    // Parse received message and dispatch as necessary
    public void parse(Message received)
    {
        
    }
    
    public void read(int toRead)
    {
    
    }
    
    public void write(int toWrite, int amount)
    {
        
    }
    
    public void close()
    {
        
    }
    
    // Respond to client
    public void send(Message toSend)
    {
        
    }
}
