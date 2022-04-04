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
    
    // Contains references to all lock holders
    protected final ArrayList<TransactionManagerWorker> lockHolders;
        
    // Indicates lock type, NONE if no one is locking READ if one or more are
    // reading WRITE if one is writing
    protected int lockType;
        
    public Lock(int initAccount)       
    {
        account = initAccount;
        
        lockHolders = new ArrayList();
        lockType = NONE;
        
    }
    
    // Acquire a lock on this account
    public synchronized void acquire(TransactionManagerWorker acquiring,
                                     int requestedLockType) 
            throws AbortedException
    {
        String logBuffer = "Set lock " + stringFromType(requestedLockType) 
                           + " on account #" + account;
        
        // Check if we can acquire the lock we want
        while (isConflict(acquiring, requestedLockType))
        {
            // Before we wait, check to see if a deadlock is forming. If there
            // is one forming, this will throw an AbortedException
            deadlockForming(acquiring, requestedLockType);
            
            try
            {    
                // Set that this transaction is now waiting on this lock
                acquiring.waiting = this;
                // Indicate that we are waiting
                acquiring.appendLog("Wait to set " 
                                    + stringFromType(requestedLockType) 
                                    + " on account #" + account);
                
                wait();
            }
            catch (InterruptedException e)
            {
                
            }                
            
            // Indicate we woke back up
            acquiring.appendLog("Woke up, again trying to set " 
                                + stringFromType(requestedLockType) 
                                + " on account #" + account);
        }

        // If the requesting transaction did not already hold any lock on this
        // account, add it to the lock holders and add this to its held locks
        if (!lockHolders.contains(acquiring))
        {
            lockHolders.add(acquiring);
            acquiring.heldLocks.add(this);
        }
                
        // Indicate if we are sharing the lock with anyone
        if (lockHolders.size() > 1)
        {
            logBuffer += ", sharing lock with:";
            
            for (TransactionManagerWorker holder : lockHolders)
            {
                if (holder != acquiring)
                {
                    logBuffer += " #" + holder.tid;
                }
            }
        }
        
        acquiring.appendLog(logBuffer);
        
        // Set type as appropriate
        lockType = requestedLockType;
    }
    
    // Release a lock on the account
    public synchronized void release(TransactionManagerWorker releasing)
    {
        // Remove this transaction
        lockHolders.remove(releasing);
        releasing.appendLog("Release " + stringFromType(lockType) 
                            + ", account #" + account);
        
        // If this was the only holder set the lock type to NONE. Note that we
        // will never need to demote from WRITE to READ because if the releasing
        // transaction held a WRITE lock it would necessarily be the only lock
        // holder at time of release meaning we will now set to NONE
        if (lockHolders.isEmpty())
        {
            lockType = NONE;
        }
        
        // Notify any waiting locks that they should try to acquire again
        notifyAll();
    }
    
    // Cannot acquire a lock if another transaction holds a write lock or if
    // we are requesting a write lock and other transactions already hold
    // read locks
    private boolean isConflict(TransactionManagerWorker acquiring,
                               int requestedLockType)
    {
        String logBuffer = "Current lock " + stringFromType(lockType) + 
                           " on account #" + account;
        
        // These were split into two conditions like this to make the logging
        // easier below
        boolean onlyHolder = lockHolders.size() == 1 
                             && lockHolders.contains(acquiring);
        boolean conflict = (lockType == WRITE) 
                            || (requestedLockType == WRITE && lockType != NONE 
                            && !onlyHolder);
        
        // Determine how to report the current situation
        if (lockType == NONE)
        {
            logBuffer += ", no holder";
        }
        else if (onlyHolder)
        {
            logBuffer += ", transaction is sole holder";
        }
        else
        {
            logBuffer += ", held by:";
            
            for (TransactionManagerWorker holder : lockHolders)
            {
                logBuffer += " #" + holder.tid;
            }
        }
        
        logBuffer += ", new lock " + stringFromType(requestedLockType);
        
        if (conflict)
        {
            logBuffer += ", conflict!";
        }
        else
        {
            logBuffer += ", no conflict";
        }
        
        acquiring.appendLog(logBuffer);
        return conflict;
    }
    
    // If the acquring transaction was told to wait, but it there is a holder of
    // this lock that is waiting on a lock the acquring transaction holds, the
    // acquiring transaction should abort to prevent deadlock
    private void deadlockForming(TransactionManagerWorker acquiring, 
                                int requestedLockType) throws AbortedException
    {
        for (TransactionManagerWorker holder : lockHolders)
        {
            if (acquiring.heldLocks.contains(holder.waiting) 
                && acquiring != holder)
            {
                acquiring.appendLog("Aborting when trying to set a " 
                                    + stringFromType(requestedLockType) 
                                    + " on account #" + account + " while"
                                    + " holding a " 
                                    + stringFromType(holder.waiting.lockType) 
                                    + " on account #" + holder.waiting.account);
                throw new AbortedException("Transaction " + acquiring.tid 
                                           + " aborted");
            }
        }    
    }

    // Get a string representation of a lock type
    public String stringFromType(int type)
    {
        switch (type)
        {
            case NONE:
                return "NONE_LOCK";
            case READ:
                return "READ_LOCK";
            case WRITE:
                return "WRITE_LOCK";
        }
        
        // If you gave it something invalid explode
        return null;
    }
}
