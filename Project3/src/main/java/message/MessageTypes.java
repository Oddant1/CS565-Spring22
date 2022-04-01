/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package message;

/**
 * Interface to define the different message types in the system. If a class
 * wants to interact with Message class objects it must implement this interface
 * 
 * @author anthony
 */
// TODO: A lot of these may end up being unnecessary
public interface MessageTypes 
{
    // When a message field isn't needed
    public static final int DEFAULT = -1;
    
    public static final int OPEN = 0;
    public static final int OPENED = 1;
        
    public static final int READ = 3;
    public static final int READ_RESPONSE = 5;
    
    public static final int WRITE = 4;
    public static final int WRITE_RESPONSE = 6;

    public static final int CLOSE = 7;
    public static final int COMMITTED = 8;
    public static final int ABORTED = 9;
    
    public static final int SHUTDOWN = 10;
}
