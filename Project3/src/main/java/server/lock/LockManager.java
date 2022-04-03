/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.lock;

import aborted.*;
import server.transaction.*;

/**
 * Used to interact with locks on accounts
 * 
 * @author anthony
 */
public class LockManager implements LockTypes
{
    private final Lock[] locks;
    
    // Create locks on all accounts with type set to NONE
    public LockManager(int numAccounts)
    {
        locks =  new Lock[numAccounts];
        
        for (int account = 0; account < numAccounts; account++)
        {
            locks[account] = new Lock(account);
        }
    }
    
    // Acquire a lock on an account if possible
    public void setLock(TransactionManagerWorker setting, int lockIndex, int requestedLockType) throws AbortedException
    {   
        Lock requestedLock = locks[lockIndex];
        
        if (!(requestedLock.lockHolders.contains(setting) && requestedLockType <= requestedLock.lockType))
        {
            setting.appendLog("Trying to set " + requestedLock.stringFromType(requestedLockType) + " on account #" + lockIndex);
            
            // Request a lock
            requestedLock.acquire(setting, requestedLockType);
            
            // If we got the lock and were not already holding a lock on this account
            // Add the lock to our held locks
            if (!setting.heldLocks.contains(requestedLock))
            {
                setting.heldLocks.add(requestedLock);
            }
        }
        else
        {
            setting.appendLog("Transaction #" + setting.tid + " already has necessary lock on account #" + lockIndex);
        }
    }
    
    // Release all locks transaction holds
    public void unLock(TransactionManagerWorker unlocking)
    {
        for (int i = 0; i < unlocking.heldLocks.size(); i++)
        {
            unlocking.heldLocks.get(i).release(unlocking);
        }
    }
}
