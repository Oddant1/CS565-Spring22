/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import util.*;
import message.*;
import aborted.*;

import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 * Provides an interface for the client to communicate with the server
 * 
 * @author anthony
 */
public class TransactionServerProxy implements MessageTypes
{
    protected ServerSocket serverSocket;
    private final SenderReceiver senderReceiver;
    
    // TID to be used in communication (Mostly just for reporting purposes) will
    // be inited by server response
    protected int oldTid = -1;
    protected int tid = -1;
        
    // Store the server info for open requests
    private final String serverIp;
    private final int serverPort;
    
    // These are the IP and port of the worker we are using on the server
    private String workerIp;
    private int workerPort;
    
    // The port will be unique to each transaction
    private final String myIp;
    protected int myPort = 0;
            
    public TransactionServerProxy(String initServerIp, int initServerPort,
                                  String initMyIp)
    {
        serverIp = initServerIp;
        serverPort = initServerPort;

        myIp = initMyIp;
        
        try
        {
            // Get a socket on an open port then read that port
            serverSocket = new ServerSocket(0);
            myPort = serverSocket.getLocalPort();
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionServerProxy.class.getName())
                    .log(Level.SEVERE, null, e);
            System.err.println("Error creating ServerSocket on port " + myPort
                               + ": IOException " + e);
            System.exit(1);
        }        
        
        senderReceiver = new SenderReceiver(serverSocket, serverIp, serverPort);
    }
    
    // Tell server to create new transaction
    public void openTransaction() throws AbortedException
    {
        oldTid = tid;
        
        // Send an open request to the server and get back our tid and the ip
        // and port of the worker we're communicating with
        Message received = handleCommunication(
                new Message(tid, OPEN, DEFAULT, DEFAULT, myIp, myPort));

        // Set our tid and the worker ip and port
        tid = received.tid;
        workerIp = received.responseIp;
        workerPort = received.responsePort;
        
        // Update where we are sending messages so they go to our worker not the
        // main server
        senderReceiver.ip = workerIp;
        senderReceiver.port = workerPort;
    }
        
    // Tell worker this transaction is reading the value of a given account
    public int read(int toRead) throws AbortedException
    {
        return handleCommunication(
                new Message(tid, READ, toRead, DEFAULT, myIp,myPort)).amount;
    }
    
    // Tell worker this transaction is writing a given value to a given account
    public void write(int toWrite, int amount) throws AbortedException
    {
        handleCommunication(
                new Message(tid, WRITE, toWrite, amount, myIp, myPort));
    }
    
    // Tell worker this transaction is closing
    public void closeTransaction() throws AbortedException
    {
        handleCommunication(
                new Message(tid, CLOSE, DEFAULT, DEFAULT, myIp, myPort));
    }
    
    // Handle worker communication in a generic way
    public Message handleCommunication(Message toSend) throws AbortedException
    {
        Message received;
        
        // Send our message and receive our response
        senderReceiver.send(toSend);
        received = senderReceiver.receive();
        
        // Throw exception if we are aborting
        if (received.type == ABORTED)
        {
            // Spaces for visual indentation
            throw new AbortedException("        Transaction " + "#" + tid 
                                       + " ABORTED due to deadlock");
        }
        
        // Return received message if we are not aborting
        return received;
    }
    
    // If we were aborted, we use this to reset our communication info so we
    // submit a new OPEN request to the server instead of trying to send it to
    // the old worker and making bad things happen
    public void resetRemoteInfo()
    {
        senderReceiver.ip = serverIp;
        senderReceiver.port = serverPort;
    }
}
