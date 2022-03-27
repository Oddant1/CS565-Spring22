package aborted;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */

/**
 *
 * @author anthony
 */
public class AbortedException extends Exception
{
    /**
     * Constructs an instance of <code>Aborted</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public AbortedException(String msg) 
    {
        super(msg);
    }
}
