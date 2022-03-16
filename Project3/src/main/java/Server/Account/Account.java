/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Server.Account;

import Server.Lock.Lock;

/**
 * Represents an account on the server. Really just needs to know who is locking
 * it and how much money is in it.
 * 
 * @author anthony
 */
public class Account 
{
    // All locks currently held on this account
    public Lock lock;
    // Balance of account
    public int balance;
    
    public Account(int initBalance)
    {
        
    }
}
