/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server.Transaction;

import java.net.ServerSocket;

/**
 * Creates the transaction, lock, and account managers then pawns work off onto
 * them`
 *
 * @author anthony
 */
public class TransactionServer 
{
    private ServerSocket socket;
    
    // We are going to want to receive a file telling us which port to open, num
    // accounts and initial balances then create all managers
    public static void main()
    {
        
    }
    
    // Await requests for new transactions and hand them off to the transaction
    // mananger. Maybe we eliminate this and just have the transaction manager
    // recevie the requsts? Then all this class does is kickstart things
    public void run()
    {
        
    }
}
