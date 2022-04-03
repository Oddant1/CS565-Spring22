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
 * Holds and manages all of the accounts. Objects interact with accounts through
 * this manager
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
    
    // Will attempt to acquire read lock on account and return value
    public int read(TransactionManagerWorker reader, int account) throws AbortedException
    {
        if (locking)
        {
            lockManager.setLock(reader, account, READ);
        }
        
        return accounts[account];
    }
    
    // Will attempt to promote to write lock on account. Value is not written
    // until we commit
    public void write(TransactionManagerWorker writer, int account, int amount) throws AbortedException
    {
        if (locking)
        {
            lockManager.setLock(writer, account, WRITE);
        }        
    }
    
    // Commit a write when a transaction is committing
    public synchronized void commitWrite(TransactionManagerWorker writer, int account, int amount)
    {
        writer.appendLog("Writing amount $" + amount + " to account #" + account);
        accounts[account] = amount;
    }
    
    // Sum the account balances before we close
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
