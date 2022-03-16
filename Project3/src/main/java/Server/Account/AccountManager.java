/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server.Account;

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
    private final Account[] accounts;
    
    public AccountManager(int numAccounts, int accountBalances)
    {
        
    }
    
    // Will attempt to acquire read lock on account and return value
    public int read(int readerTid, int accountIndex)
    {
        
    }
    
    
    // Will attempt to promote to write lock on account and write value
    public void write(int readerTid, int amount, int accountIndex)
    {
        
    }
}
