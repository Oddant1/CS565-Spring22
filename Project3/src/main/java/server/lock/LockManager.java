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
    
    public LockManager(int numAccounts)
    {
        locks =  new Lock[numAccounts];
        
        for (int account = 0; account < numAccounts; account++)
        {
            locks[account] = new Lock(account);
        }
    }
    
    // Acquire a lock on an account if possible
    public void setLock(TransactionManagerWorker setting, int beingSet, int requestedLockType) throws AbortedException
    {        
        locks[beingSet].acquire(setting, requestedLockType);
    }
    
    // Release all locks transaction holds
    public void unLock(TransactionManagerWorker unlocking)
    {
        for (int i = 0; i < unlocking.heldLocks.size(); i++)
        {
            locks[unlocking.heldLocks.get(i)].release(unlocking);
        }
    }
}
