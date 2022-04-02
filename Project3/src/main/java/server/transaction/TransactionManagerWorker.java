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
    private final AccountManager accountManager;
    private final LockManager lockManager;
    
    private ServerSocket serverSocket;
    private final SenderReceiver senderReceiver;
    
    // This will likely also need a reference to the account and lock managers
    // All locks currently held by the transaction
    public ArrayList<Integer> heldLocks;
    // We can only be waiting on one lock at a time. This needs to be public w
    public int waiting = -1;
    
    public final int tid;
    
    private final String myIp;
    private int myPort = 0;
    
    private final String clientIp;
    private final int clientPort;
    
    private final HashMap<Integer, Integer> writes;
    
    public TransactionManagerWorker(int initTid, String initMyIp,
                                    String initClientIp, int initClientPort,
                                    AccountManager initAccountManager,
                                    LockManager initLockManager)
    {
        heldLocks = new ArrayList();
        writes = new HashMap();
        
        tid = initTid;
        
        myIp = initMyIp;
        
        clientIp = initMyIp;
        clientPort = initClientPort;
        
        accountManager = initAccountManager;
        lockManager = initLockManager;
        
        try
        {
            // Open our socket on an open port then read that port
            serverSocket = new ServerSocket(0);
            myPort = serverSocket.getLocalPort();
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionManagerWorker.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed to open server socket on port " + myPort + ".");
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
        senderReceiver.send(new Message(tid, OPENED, DEFAULT, DEFAULT, myIp, myPort));
        
        // TODO: Change this condition
        while (isRunning)
        {
            received = senderReceiver.receive();
            
            try
            {
                toSend = parse(received);
            }
            catch (AbortedException e)
            {
                toSend = new Message(tid, ABORTED, DEFAULT, DEFAULT, myIp, myPort);
            }
                   
            senderReceiver.send(toSend);
            
            // If we were aborted or we finished then exit
            if (toSend.type == ABORTED || toSend.type == COMMITTED)
            {
                isRunning = false;
            }
        }
        
        lockManager.unLock(this);
        
        // Need to close this or we have a potential port leak
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionManagerWorker.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Failed to close server socket on port: " + myPort + ".");
            System.exit(1);
        } 
    }
    
    // Parse received message and dispatch as necessary
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
                break;
        }
        
        return toSend;
    }
    
    public Message read(Message received) throws AbortedException
    {        
        int account = received.account;
        int amount;
        
        if (writes.containsKey(account))
        {
            amount = writes.get(account);
        }
        else
        {
            amount = accountManager.read(this, account);
        }
            
        // If we got here we have a read lock
        if (!heldLocks.contains(account))
        {
            heldLocks.add(account);
        }
        
        return new Message(tid, READ_RESPONSE, account, amount, myIp, myPort);
    }
    
    public Message write(Message received) throws AbortedException
    {
        // Account to write to
        int account = received.account;
        // Amount to write
        int amount = received.amount;
        
        // Obtain permission to write to account
        accountManager.write(this, account, amount);
        
        // If we got here we have a write lock
        if (!heldLocks.contains(account))
        {
            heldLocks.add(account);
        }
        
        writes.put(account, amount);
        
        return new Message(tid, WRITE_RESPONSE, account, amount, myIp, myPort);
    }
    
    public Message close()
    {              
        for (int account : writes.keySet())
        {            
            accountManager.commitWrite(account, writes.get(account));
        }
        
        System.out.println("COMMITTED Transaction " + tid + " completed");
        return new Message(tid, COMMITTED, DEFAULT, DEFAULT, myIp, myPort);
    }
}
