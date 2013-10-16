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
	 * "<VAR>".
	 * @param varmap
	 * 			The map of translation-time variable name keys to anonimised
	 * 			variable number.
	 * @return anonimised name
	 */
	public String anonimised(Map<String,Integer> varmap);
	
	/**
	 * Using this function as the root, produce an anonimised name for this
	 * function such that all variables joined before or in this function are
	 * replaced with the integer dictated by the order in which they first appear
	 * in the function. All other variables are replaced with the string "<VAR>".
	 * @return anonimised name
	 */
	public String anonimised();


	/**
	 * Informs the function of the mapping between the runtime variable names it
	 * will receive and the translation-time variables it was created with.  
	 * @param varmap -
	 * 			The map of translation-time variable name keys to runtime
	 * 			variable name values.
	 */
	public void mapVariables(Map<String, String> varmap);
}
