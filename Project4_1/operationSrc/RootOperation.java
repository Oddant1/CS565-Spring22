/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dynNet.operationsImpl;

import dynNet.dynCalculator.Operation;

/**
 * 
 * @author anthony
 */
public class RootOperation implements Operation {	
	public float calculate(float firstNumber, float secondNumber){
		return (float) Math.pow(firstNumber, 1 / secondNumber);
	}
}
