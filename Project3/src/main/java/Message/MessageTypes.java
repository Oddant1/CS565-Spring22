/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package Message;

/**
 * Interface to define the different message types in the system. If a class
 * wants to interact with Message class objects it must implement this interface
 * 
 * @author anthony
 */
public interface MessageTypes 
{
    public static final int OPEN = 0;
    public static final int CLOSE = 1;
    public static final int ABORT = 3;
    
    public static final int READ = 3;
    public static final int WRITE = 4;
    
    public static final int READ_RESPONSE = 5;
    
    public static final int COMMITTED = 6;
    public static final int ABORTED = 7;
    
    public static final int SHUTDOWN = 8;
}
