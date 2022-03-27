/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.account;

import java.util.*;

/**
 * Holds and manages all of the accounts. Objects interact with accounts through
 * this manager
 * 
 * @author anthony
 */
public class AccountManager 
{
    // We can make this a final array because we will have an unchanging number
    // of accounts
    private final int[] accounts;
    
    public AccountManager(int numAccounts, int accountBalances)
    {
        accounts = new int[numAccounts];
        Arrays.fill(accounts, accountBalances);
    }
    
    // Will attempt to acquire read lock on account and return value
    public int read(int readerTid, int account)
    {
        // Skips the locking for now
        return accounts[account];
    }
    
    // Will attempt to promote to write lock on account and write value
    public void write(int readerTid, int amount, int accountIndex)
    {
        
    }
}
