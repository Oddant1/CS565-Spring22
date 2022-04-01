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
    public int waiting;
    
    
    public final int tid;
    
    private final String myIp;
    private int myPort = 0;
    
    private final String clientIp;
    private final int clientPort;
    
    private final ArrayList<int[]> writes;
    
    public TransactionManagerWorker(int initTid, String initMyIp,
                                    String initClientIp, int initClientPort,
                                    AccountManager initAccountManager,
                                    LockManager initLockManager)
    {
        writes = new ArrayList();
        
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
        Message received = null;
        Message toSend = null;
        
        // TODO: Change this condition
        while (true)
        {
            senderReceiver.receive();
            
            try
            {
                toSend = parse(received);
            }
            catch (AbortedException e)
            {
                toSend = new Message(tid, ABORTED, DEFAULT, DEFAULT, myIp, myPort);
            }
            senderReceiver.send(toSend);
        }
    }
    
    // Parse received message and dispatch as necessary
    public Message parse(Message received) throws AbortedException
    {
        Message toSend = null;
        
        switch (received.type)
        {
            case OPEN:
                toSend = open(received);
                break;
            case CLOSE:
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
        
    public Message open(Message received)
    {
        return new Message(tid, OPENED, DEFAULT, DEFAULT, myIp, myPort);
    }
    
    public Message read(Message received) throws AbortedException
    {        
        int toRead = received.account;
        // Read the value of the account (requires a read lock if we are using
        // locking)
        int amountRead = accountManager.read(this, toRead);
        
        // If we got here we have a read lock
        heldLocks.add(toRead);
        
        return new Message(tid, READ_RESPONSE, toRead, amountRead, myIp, myPort);
    }
    
    public Message write(Message received) throws AbortedException
    {
        // Account to write to
        int account = received.account;
        // Amount to write
        int amount = received.amount;
        
        // Holds information for write to be commited first account then amount 
        int write[] = new int[2];
        
        // Obtain permission to write to account
        accountManager.write(this, account, amount);
        
        // Create our write
        write[0] = account;
        write[1] = amount;
        
        // Store our write
        writes.add(write);
        
        return new Message(tid, WRITE_RESPONSE, account, amount, myIp, myPort);
    }
    
    public Message close()
    {
        int account;
        int amount;
        
        lockManager.unLock(this);
        
        for (int[] write : writes)
        {
            account = write[0];
            amount = write[1];
            
            accountManager.commitWrite(account, amount);
        }
        
        return new Message(tid, COMMITTED, DEFAULT, DEFAULT, myIp, myPort);
    }
}
