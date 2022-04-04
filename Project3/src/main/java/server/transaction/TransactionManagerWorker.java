/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.transaction;

import util.*;
import aborted.*;
import message.*;
import server.lock.*;
import server.account.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * This basically represents a single transaction on the server
 * 
 * @author anthony
 */
public class TransactionManagerWorker extends Thread implements MessageTypes
{
    // Log of all actions related to this transaction
    private String log = "";
    
    private final AccountManager accountManager;
    private final LockManager lockManager;
    private final TransactionManager transactionManager;
    
    private ServerSocket serverSocket;
    private final SenderReceiver senderReceiver;
    
    // All locks currently held by the transaction
    public ArrayList<Lock> heldLocks;
    // We can only be waiting on one lock at a time
    public Lock waiting;
    
    public final int tid;
    
    private final String myIp;
    private int myPort = 0;
    
    // The info of the client proxy we are talking to
    private final String clientIp;
    private final int clientPort;
    
    // The account we are writing to mapped to the value we are writing
    private final HashMap<Integer, Integer> writes;
    
    public TransactionManagerWorker(int initTid, String initMyIp,
                                    String initClientIp, int initClientPort,
                                    AccountManager initAccountManager,
                                    LockManager initLockManager,
                                    TransactionManager initTransactionManager)
    {
        heldLocks = new ArrayList();
        writes = new HashMap();
        
        tid = initTid;
        
        myIp = initMyIp;
        
        clientIp = initMyIp;
        clientPort = initClientPort;
        
        accountManager = initAccountManager;
        lockManager = initLockManager;
        transactionManager = initTransactionManager;
        
        try
        {
            // Open our socket on an open port then read that port
            serverSocket = new ServerSocket(0);
            myPort = serverSocket.getLocalPort();
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionManagerWorker.class.getName())
                    .log(Level.SEVERE, null, e);
            System.out.println("Failed to open server socket on port " 
                               + myPort + ".");
            System.exit(1);
        }        
        
        senderReceiver = new SenderReceiver(serverSocket, clientIp, clientPort);
    }
    
    // Receive requests from our client
    @Override
    public void run()
    {
        boolean isRunning = true;
        
        Message received = null;
        Message toSend = null;
        
        // Tell our client that we are open
        senderReceiver.send(
                new Message(tid, OPENED, DEFAULT, DEFAULT, myIp, myPort));
        appendLog("OPEN TRANSACTION #" + tid);

        // Get requests
        while (isRunning)
        {
            received = senderReceiver.receive();
            
            try
            {
                // parse requests and generate responses
                toSend = parse(received);
            }
            catch (AbortedException e)
            {
                toSend = new Message(tid, ABORTED, DEFAULT, DEFAULT, myIp,
                                     myPort);
            }
                   
            senderReceiver.send(toSend);
            
            // If we were aborted or we finished then exit
            if (toSend.type == ABORTED || toSend.type == COMMITTED)
            {
                isRunning = false;
            }
        }
        
        // Need to remove our locks
        lockManager.unLock(this);
        
        // Need to close this or we have a potential port leak
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionManagerWorker.class.getName())
                    .log(Level.SEVERE, null, e);
            System.out.println("Failed to close server socket on port: " 
                               + myPort + ".");
            System.exit(1);
        }
        
        // Add our final bit to the log then add our log to the appropriate 
        // location
        if (toSend.type == COMMITTED)
        {        
            appendLog("COMMIT_TRANSACTION #" + tid);
            transactionManager.addCommitted(log);
        }
        else
        {
            appendLog("ABORT_TRANSACTION during " 
                      + (received.type == READ ? "READ_REQUEST" : 
                         "WRITE_REQUEST") 
                      + " due to deadlock");
            transactionManager.addAborted(log);
        }
    }
    
    // Read type of received message and dispatch as necessary
    public Message parse(Message received) throws AbortedException
    {
        Message toSend = null;
        
        switch (received.type)
        {
            case CLOSE:
                toSend = close();
                break;
            case READ:
                toSend = read(received);
                break;
            case WRITE:
                toSend = write(received);
                break;
            default:
                System.out.println("Received invalid message type for worker: " 
                                   + received.type);
                System.exit(1);
        }
        
        return toSend;
    }
    
    public Message read(Message received) throws AbortedException
    {        
        // Account to read
        int account = received.account;
        // Amount read
        int amount;
        
        appendLog("READ_REQUEST account #" + account);
        
        // If we are going to overwrite the value in the account read the value
        // we are about to write
        if (writes.containsKey(account))
        {
            amount = writes.get(account);
            appendLog("We have a queued write of $" + amount + " on account #" 
                      + account);
        }
        // Otherwise we need to get a lock and read the account
        else
        {
            amount = accountManager.read(this, account);
            appendLog("Read balance $" + amount + " from account #" + account);
        }
        
        return new Message(tid, READ_RESPONSE, account, amount, myIp, myPort);
    }
    
    public Message write(Message received) throws AbortedException
    {
        // Account to write to
        int account = received.account;
        // Amount to write
        int amount = received.amount;
        
        appendLog("WRITE_REQUEST account #" + account + ", new balance $" 
                  + amount);
        
        // Obtain permission to write to account
        accountManager.write(this, account, amount);
                
        // Add our write to be committed later if we commit
        writes.put(account, amount);
        
        return new Message(tid, WRITE_RESPONSE, account, amount, myIp, myPort);
    }
    
    // Close and commit the transaction
    public Message close()
    {              
        // Commit all of our writes
        for (int account : writes.keySet())
        {            
            accountManager.commitWrite(this, account, writes.get(account));
        }
        
        return new Message(tid, COMMITTED, DEFAULT, DEFAULT, myIp, myPort);
    }
    
    // Appends to the log and adds a newline and action #
    public void appendLog(String addition)
    {
        log += transactionManager.getCount() + " " + addition + "\n";
    }
}
