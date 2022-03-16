/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package Server.Lock;

/**
 * Indicates the types of locks that may be held, NONE may be superfluous
 * 
 * @author anthony
 */
public interface LockTypes 
{
    final int NONE = 0;
    final int READ = 1;
    final int WRITE = 2;
}
