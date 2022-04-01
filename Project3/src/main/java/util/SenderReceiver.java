/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package util;

import message.*;

import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 *
 * @author anthony
 */
public class SenderReceiver implements MessageTypes
{
    
    private final ServerSocket serverSocket;
    
    public String ip;
    public int port;
    
    public SenderReceiver(ServerSocket initServerSocket, String initIp,
                          int initPort)
    {
        serverSocket = initServerSocket;
        
        ip = initIp;
        port = initPort;
    }

    // Send our message
    public void send(Message toSend)
    {
        Socket socket;
        ObjectOutputStream toRemote;
        
        try
        {
            socket = new Socket(ip, port);
            
            toRemote = new ObjectOutputStream(socket.getOutputStream());
            toRemote.writeObject(toSend);
            
            socket.close();
        }
        catch (IOException e)
        {
            Logger.getLogger(SenderReceiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error sending message: IOException " + e);
            System.exit(1);
        }   
    }

    // Receive a rmessage
    public Message receive()
    {
        Socket socket;
        
        ObjectInputStream fromRemote;
        Message received = null;
                
        try
        {
            socket = serverSocket.accept();
            
            fromRemote = new ObjectInputStream(socket.getInputStream());
            received = (Message) fromRemote.readObject();
            
            socket.close();
        }
        catch (IOException e)
        {
            Logger.getLogger(SenderReceiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Error receiving message: IOException " + e);
            System.exit(1);   
        }
        catch (ClassNotFoundException e)
        {
            Logger.getLogger(SenderReceiver.class.getName()).log(Level.SEVERE, null, e);
            System.err.println("Class not found for some reason: ClassNotFoundException " + e);
            System.exit(1);
        }
        
        return received;
    }
}
