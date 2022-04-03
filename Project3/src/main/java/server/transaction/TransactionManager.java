/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.transaction;

import message.*;
import server.lock.*;
import server.account.*;

import java.util.*;

/**
 * Creates new transaction workers that communicate with a given transaction on
 * the client
 *
 * @author anthony
 */
public class TransactionManager 
{
    // Contain logs of committed and aborted transactions respectively
    public ArrayList<String> committedLog;
    public ArrayList<String> abortedLog;
    
    // Monotonically increases as transactions access it to number their print
    // Gives a strict ordering to the output lines
    public int count = 0;
    
    private final AccountManager accountManager;
    private final LockManager lockManager;
    
    private final String myIp;
    private int tid = 0;

    public TransactionManager(AccountManager initAccountManager,
                              LockManager initLockManager,
                              String initMyIp)
    {
        committedLog = new ArrayList();
        abortedLog = new ArrayList();
        
        accountManager = initAccountManager;
        lockManager = initLockManager;
        
        myIp = initMyIp;
    }
    
    // Spawns a worker
    public void createTransaction(Message newTransactionRequest)
    {   
        final String clientIp = newTransactionRequest.responseIp;
        final int clientPort = newTransactionRequest.responsePort;
        
        new TransactionManagerWorker(tid, myIp, clientIp, clientPort,
                                     accountManager, lockManager, this).start();
        tid++;
    }
    
    public synchronized void addCommitted(String log)
    {
        committedLog.add(log);
    }
    
    public synchronized void addAborted(String log)
    {
        abortedLog.add(log);
    }
    
    public synchronized int getCount()
    {
        int currentCount = count;
        
        count++;
        
        return currentCount;
    }
}
