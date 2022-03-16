/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Message;

import java.io.Serializable;

/**
 * Objects of this type are used to communicate between client and server
 * 
 * @author anthony
 */
public class Message implements Serializable, MessageTypes
{
    // Type of request
    final int type;
    // Account implicated (if relevant)
    final int account;
    // Amount written (if relevant)
    final int amount;

    // Connection info of sending entity (info the receiver will be responding
    // to)
    final String responseIp;
    final int responsePort;
    
    public Message(int initTid, int initType, int initAccount, int initAmount,
                   String initResponseIp, int initResponsePort)
    {
        
    }
}
