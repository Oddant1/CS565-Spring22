/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package server.lock;

import java.util.ArrayList;

/**
 * Each account has a lock that may be acquired and released by transactions
 * 
 * @author anthony
 */
public class Lock implements LockTypes
{
    // Contains TIDs of lock holders. Maybe this should be the 
    // TransactionWorkerManagers themselves?
    private final ArrayList<Integer> lockHolders;
    // Indicates lock type, NONE if no one is locking READ if one or more are
    // reading WRITE if one is writing
    private int lockType;
    
    public Lock()
    {
        lockHolders = new ArrayList();
        lockType = NONE;
    }
    
    // Acquire a lock on an account
    public synchronized void acquire(int acquiring, int requestedLockType)
    {
        // Check if we can acquire the lock we want
        while (isConflict(acquiring, requestedLockType))
        {
            try
            {
                // Wait if we can't acquire the lock yet due to conflicts
                wait();
            }
            catch (InterruptedException e)
            {
                // We want the interrupt to end up happening
            }
        }

        // If we are requesting a WRITE lock, we can only get here if we are the
        // only lock holder. If we are requesting a READ lock, we can only get
        // here if we are the only holder or the current lock is READ
        if (!lockHolders.contains(acquiring))
        {
            lockHolders.add(acquiring);
        }
        
        lockType = requestedLockType;
    }
    
    // Release a lock on the account
    public synchronized void release(int releasing)
    {
        // Release this transaction and set the lock type to NONE if this was
        // the only transaction
        lockHolders.remove(releasing);
        
        if (lockHolders.isEmpty())
        {
            lockType = NONE;
        }
        
        // Notify any waiting locks that they should check for conflicts again
        notifyAll();
    }
    
    public boolean isConflict(int acquiring, int requestedLockType)
    {
        // Cannot acquire a lock if another transaction holds a write lock or if
        // we are requesting a write lock and other transactions already hold
        // read locks
        return !(lockHolders.isEmpty() || 
                 (lockHolders.contains(acquiring) && lockHolders.size() == 1) || 
                 (lockType == READ && requestedLockType == READ));
    }
}
