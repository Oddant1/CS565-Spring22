/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package message;

import java.io.Serializable;

/**
 * Objects of this type are used to communicate between client and server
 * 
 * @author anthony
 */
public class Message implements Serializable, MessageTypes
{
    // Id of implicated transaction
    public final int tid;
    
    // Type of request
    public final int type;
    // Account implicated (if relevant)
    public final int account;
    // Amount to write or amount read
    public final int amount;

    // Connection info of sending entity (info the receiver will be responding
    // to)
    public final String responseIp;
    public final int responsePort;
    
    public Message(int initTid, int initType, int initAccount, int initAmount,
                   String initResponseIp, int initResponsePort)
    {
        tid = initTid;
        
        type = initType;
        account = initAccount;
        amount = initAmount;
        
        responseIp = initResponseIp;
        responsePort = initResponsePort;
    }
}
