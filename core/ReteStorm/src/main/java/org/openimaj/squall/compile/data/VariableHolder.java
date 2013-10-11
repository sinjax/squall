package org.openimaj.squall.compile.data;

import java.util.List;
import java.util.Map;

import org.openimaj.util.function.Function;

/**
 * Apply a function to some input, producing an appropriate result.
 * Also provide a method to become anonymised (i.e. present the underlying {@link Function} as a variable independant string)
 * and become annonimised with certained variables set by a {@link Map}
 */
public interface VariableHolder{
	/**
	 * @return the variables used in this function
	 */
	public List<String> variables();
	
	/**
	 * Given a varmap, produce an anonimised name for this function such that
	 * all variables this function contains in the map are replaces with the 
	 * integer specified, and all other variables are replaced with the string
	 * "<VAR>"
	 * @param varmap
	 * @return anonimised name
	 */
	public String anonimised(Map<String,Integer> varmap);
	
	/**
	 * The name of this function with all variables replaced with "<VAR>"
	 * @param varmap
	 * @return anonimised name
	 */
	public String anonimised();
}
