/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Server.Lock;

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
    private ArrayList<Integer> lockHolders;
    // Indicates lock type, NONE if no one is locking READ if one or more are
    // reading WRITE if one is writing
    private int lockType;
    
    public Lock()
    {
        
    }
    
    // Acquire a lock on an account
    public synchronized void acquire(int acquiring)
    {
        
    }
    
    // Release a lock on the account
    public synchronized void release(int releasing)
    {
        
    }
}
