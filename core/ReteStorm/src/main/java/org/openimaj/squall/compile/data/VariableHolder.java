package org.openimaj.squall.compile.data;

import java.util.ArrayList;
import java.util.List;
import org.openimaj.util.function.Function;

/**
 * Allows access to the variables utilised by the {@link VariableHolder}, both the underlying variables used during processing,
 * and their equivalent variables in last rule to make use of it during orchestration.
 * Also provide a method to access an anonimised representation of the {@Link VariableHolder} (e.g. present an underlying {@link Function} as
 * an implementation and rule independent string), as well as a method to access the list of contributing atomic {@link VariableHolder}s.
 */
public abstract class VariableHolder{
	
	private List<String> variables;
	
	/**
	 * 
	 */
	public VariableHolder(){
		this.variables = new ArrayList<String>();
	}
	
	/**
	 * @return the underlying variables used in this function, ordered by appearance in the function output.
	 */
	public String[] variables(){
		String[] bvars = new String[this.variables.size()];
		bvars = this.variables.toArray(bvars);
		return bvars;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public boolean addVariable(String name){
		return this.variables.add(name);
	}
	
	/**
	 * @param index
	 * @return
	 */
	public String getVariable(int index){
		return this.variables.get(index);
	}
	
	/**
	 * @return
	 */
	public int varCount(){
		return this.variables.size();
	}
	
	/**
	 * @param name
	 * @return
	 */
	public int indexOfVar(String name){
		return this.variables.indexOf(name);
	}
	
	/**
	 * Produces a representative identifier of this function.
	 * @return representative identifier
	 */
	public abstract String identifier();
	
	/**
	 * @return
	 */
	public boolean resetVars(){
		this.variables.clear();
		return true;
	}
	
	/**
	 * @return
	 */
	public boolean wipeVars(){
		this.variables = null;
		return true;
	}
	
}
