/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.transaction;

import message.*;
import server.lock.*;
import server.account.*;

/**
 * Creates new transaction workers that communicate with a given transaction on
 * the client
 *
 * @author anthony
 */
public class TransactionManager 
{       
    private final AccountManager accountManager;
    private final LockManager lockManager;
    
    private final String myIp;
    private int tid = 0;

    public TransactionManager(AccountManager initAccountManager,
                              LockManager initLockManager,
                              String initMyIp)
    {
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
                                     accountManager, lockManager).start();
        tid++;
    }
}
