/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.lock;

import server.transaction.TransactionManagerWorker;

import java.util.Arrays;

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
        Arrays.fill(locks, new Lock());
    }
    
    // Acquire a lock on an account if possible
    public void setLock(int settingTid, int beingSet, int requestedLockType)
    {
        locks[beingSet].acquire(settingTid, requestedLockType);
    }
    
    // Release all locks transaction holds
    public synchronized void unLock(TransactionManagerWorker unlocking)
    {
        for (int i = 0; i < unlocking.heldLocks.size(); i++)
        {
            unlocking.heldLocks.get(i).release(unlocking.tid);
        }
    }
}
