/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Message.MessageTypes;

import java.net.Socket;
import java.net.ServerSocket;

/**
 * Provides an interface for the client to communicate with the server
 * 
 * @author anthony
 */
public class TransactionServerProxy extends Thread implements MessageTypes
{
    private final String serverIp;
    private final int serverPort;
    
    // The port will be unique to each transaction
    private final String myIp;
    private final int myPort;
            
    public TransactionServerProxy(String initServerIp, int initServerPort,
                                  String initMyIp, int initMyPort)
    {
        
    }
    
    // Tell server to create new transaction
    public void openTransaction()
    {
        
    }
        
    // Tell server this transaction is reading the value of a given account
    public void read()
    {
    
    }
    
    // Tell server this transaction is writing a given value to a given account
    public void write()
    {
        
    }
    
    // Tell server this transaction is closing
    public void closeTransaction()
    {
        
    }
}
