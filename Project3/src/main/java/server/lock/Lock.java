/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.lock;

import aborted.*;
import server.transaction.*;

import java.util.ArrayList;

/**
 * Each account has a lock that may be acquired and released by transactions
 * 
 * @author anthony
 */
public class Lock implements LockTypes
{
    // The account this lock is locking
    private final int account;
    
    // Contains TIDs of lock holders. Maybe this should be the 
    // TransactionWorkerManagers themselves?
    private final ArrayList<TransactionManagerWorker> lockHolders;
    
    // TIDs of transactions waiting for lock
    private final ArrayList<TransactionManagerWorker> waiting;
    
    // Indicates lock type, NONE if no one is locking READ if one or more are
    // reading WRITE if one is writing
    private int lockType;
    
    public Lock(int initAccount)       
    {
        account = initAccount;
        
        lockHolders = new ArrayList();
        waiting = new ArrayList();
        lockType = NONE;
    }
    
    // Acquire a lock on an account
    public synchronized void acquire(TransactionManagerWorker acquiring, int requestedLockType) throws AbortedException
    {
        // Check if we can acquire the lock we want
        while (isConflict(acquiring, requestedLockType))
        {
            try
            {
                // Will thro an AbortedException if it finds a deadloock forming
                deadlockForming(acquiring);
                
                // Wait if we can't acquire the lock yet due to conflicts
                waiting.add(acquiring);
                // Set that this transaction is now waiting on a lock for this
                // lock's account
                acquiring.waiting = account;
                                
                wait();
            }
            catch (InterruptedException e)
            {
                // We want the interrupt to end up happening
                waiting.remove(acquiring);
            }
        }

        // If we are requesting a WRITE lock, we can only get here if we are the
        // only lock holder. If we are requesting a READ lock, we can only get
        // here if we are the only holder or the current lock is READ
        if (!lockHolders.contains(acquiring))
        {
            lockHolders.add(acquiring);
        }
        
        // Set type as appropriate
        lockType = requestedLockType;
    }
    
    // Release a lock on the account
    public synchronized void release(TransactionManagerWorker releasing)
    {
        // Release this transaction and set the lock type to NONE if this was
        // the only transaction
        lockHolders.remove(releasing);
        
        // If this was the only holder set the lock type to NONE. Note that we
        // will never need to demote from WRITE to READ because if the releasing
        // transaction held a WRITE lock it would necessarily be the only lock
        // holder at time of release
        if (lockHolders.isEmpty())
        {
            lockType = NONE;
        }
        
        // Notify any waiting locks that they should check for conflicts again
        notifyAll();
    }
    
    // Cannot acquire a lock if another transaction holds a write lock or if
    // we are requesting a write lock and other transactions already hold
    // read locks
    public boolean isConflict(TransactionManagerWorker acquiring, int requestedLockType)
    {
        return !(lockHolders.isEmpty() || 
                 (lockHolders.contains(acquiring) && lockHolders.size() == 1) || 
                 (lockType == READ && requestedLockType == READ));
    }
    
    // If the acquring transaction was told to wait, but it there is a holder of
    // this lock that is waiting on a lock the acquring transaction holds, the
    // acquiring transaction should abort to prevent deadlock
    public void deadlockForming(TransactionManagerWorker acquiring) throws AbortedException
    {
        for (TransactionManagerWorker holder : lockHolders)
        {
            if (acquiring.heldLocks.contains(holder.waiting))
            {
                throw new AbortedException("Transaction " + acquiring.tid + " aborted");
            }
        }        
    }
}
