/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.account;

import aborted.*;
import server.lock.*;
import server.transaction.*;

import java.util.*;

/**
 * Holds and manages all of the accounts. Transactions interact with accounts 
 * through this manager
 * 
 * @author anthony
 */
public class AccountManager implements LockTypes
{
    // We can make this a final array because we will have an unchanging number
    // of accounts
    public final int[] accounts;
    private final LockManager lockManager;
  
    // Whether to request locks on accounts or not
    private final boolean locking;
    
    public AccountManager(int numAccounts, int accountBalances, 
                          LockManager initLockManager, boolean initLocking)
    {
        accounts = new int[numAccounts];
        Arrays.fill(accounts, accountBalances);
        
        lockManager = initLockManager;
        locking = initLocking;
    }
    
    // Read value of account
    public int read(TransactionManagerWorker reader, int account)
            throws AbortedException
    {
        // Request read lock if applicable
        if (locking)
        {
            lockManager.setLock(reader, account, READ);
        }
        
        return accounts[account];
    }
    
    // Requests a write lock on an account. Does nothing if we are not locking
    public void write(TransactionManagerWorker writer, int account, int amount)
            throws AbortedException
    {
        if (locking)
        {
            lockManager.setLock(writer, account, WRITE);
        }        
    }
    
    // Commit a write when a transaction is committing
    public synchronized void commitWrite(TransactionManagerWorker writer, 
                                         int account, int amount)
    {
        writer.appendLog("Writing amount $" + amount + " to account #" 
                         + account);
        // This is where we actually set the value a transaction is writing
        accounts[account] = amount;
    }
    
    // Sum the account balances
    public int sumAccounts()
    {
        int sum = 0;
        
        for (int account : accounts)
        {
            sum += account;
        }
        
        return sum;
    }
}
