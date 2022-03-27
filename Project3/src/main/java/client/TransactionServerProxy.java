/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

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
public class TransactionServerProxy extends Thread implements MessageTypes
{
    private ServerSocket serverSocket;
    
    // TID to be used in communication (Mostly just for reporting purposes) will
    // be inited by server response
    private int tid = -1;
    
    // These will init to the actual server then be overwritten by the info for
    // the relevant worker
    private String serverIp;
    private int serverPort;
    
    // The port will be unique to each transaction
    private final String myIp;
    private final int myPort;
            
    public TransactionServerProxy(String initServerIp, int initServerPort,
                                  String initMyIp, int initMyPort)
    {
        serverIp = initServerIp;
        serverPort = initServerPort;
        
        myIp = initMyIp;
        myPort = initMyPort;
        
        try
        {
            serverSocket = new ServerSocket(myPort);
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionServerProxy.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error creating ServerSocket on port " + myPort + ": IOException " + e);
            System.exit(1);
        }
    }
    
    // Tell server to create new transaction
    public void openTransaction() throws AbortedException
    {
        Message received = handleCommunication(new Message(tid, OPEN, 0, 0, myIp, myPort));

        tid = received.tid;
        serverIp = received.responseIp;
        serverPort = received.responsePort;
    }
        
    // Tell server this transaction is reading the value of a given account
    public int read(int toRead) throws AbortedException
    {
        return handleCommunication(new Message(tid, READ, toRead, 0, myIp, myPort)).amount;       
    }
    
    // Tell server this transaction is writing a given value to a given account
    public void write(int toWrite, int amount) throws AbortedException
    {
        handleCommunication(new Message(tid, WRITE, toWrite, amount, myIp, myPort));
    }
    
    // Tell server this transaction is closing
    public void closeTransaction() throws AbortedException
    {
        handleCommunication(new Message(tid, CLOSE, 0, 0, myIp, myPort));
    }
    
    // Handle server communication in a generic way
    public Message handleCommunication(Message toSend) throws AbortedException
    {
        Message received;
        
        // Send our message and receive our response
        send(toSend);
        received = receive();
        
        // Throw exception if we are aborting
        if (received.type == ABORTED)
        {
            throw new AbortedException("Transaction aborted: " + tid);
        }
        
        // Return received message if we are not aborting
        return received;
    }
    
    // Send our message to the server
    public void send(Message toSend)
    {
        Socket socket;
        ObjectOutputStream toServer;
        
        try
        {
            socket = new Socket(serverIp, serverPort);
            
            toServer = new ObjectOutputStream(socket.getOutputStream());
            toServer.writeObject(toSend);
            
            socket.close();
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionServerProxy.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error sending message: IOException " + e);
            System.exit(1);
        }   
    }

    // Receive a response from the server
    public Message receive()
    {
        Socket socket;
        ObjectInputStream fromServer;
        Message received = null;
        
        try
        {
            socket = serverSocket.accept();
            fromServer = new ObjectInputStream(socket.getInputStream());
            received = (Message) fromServer.readObject();
        }
        catch (IOException e)
        {
            Logger.getLogger(TransactionServerProxy.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error receiving message: IOException " + e);
            System.exit(1);   
        }
        catch (ClassNotFoundException e)
        {
            Logger.getLogger(TransactionServerProxy.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Class not found for some reason: ClassNotFoundException " + e);
            System.exit(1); 
        }
        
        return received;
    }
}
