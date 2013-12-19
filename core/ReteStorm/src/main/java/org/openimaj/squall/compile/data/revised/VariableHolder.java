package org.openimaj.squall.compile.data.revised;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openimaj.util.function.Function;

/**
 * Allows access to the variables utilised by the {@link VariableHolder}, both the underlying variables used during processing,
 * and their equivalent variables in last rule to make use of it during orchestration.
 * Also provide a method to access an anonimised representation of the {@Link VariableHolder} (e.g. present an underlying {@link Function} as
 * an implementation and rule independent string), as well as a method to access the list of contributing atomic {@link VariableHolder}s.
 */
public interface VariableHolder{
	/**
	 * @return the underlying variables used in this function, ordered by appearance in the function output.
	 */
	public List<String> baseVariables();
	
	/**
	 * @return the variables used in the current rule's use of this function, ordered by appearance in the function output.
	 */
	public List<String> ruleVariables();
	
	/**
	 * @return
	 * 		A mapping between the variables used in a given rule's use of this {@link VariableHolder} and their equivalent
	 * 		underlying variables.
	 */
	public Map<String, String> ruleToBaseVarMap();
	
	/**
	 * Replaces this {@link VariableHolder}'s ruleToBaseVarMap (returned by ruleToBaseVarMap()) with the variable varmap.
	 * @param varmap
	 * 		Replacement ruleToBaseVarMap
	 */
	public boolean mirrorInRule(VariableHolder toMirror);
	
	/**
	 * Produces an anonimised representation of this function such that all variables are
	 * replaced with a "?" followed by the integer dictated by the order in which they first appear
	 * in the function.
	 * @return anonimised name
	 */
	public String anonimised();
	
	/**
	 * Produces an representation of this function such that all variables are
	 * replaced with the string dictated by the variable map in varmap.
	 * @param varmap
	 * 		Map of underlying variable names for this {@link VariableHolder} mapped to the desired variable names.
	 * @return anonimised name
	 */
	public String anonimised(Map<String, String> varmap);
	
	/**
	 * @return The collection of atomic {@link VariableHolder}s that contribute to this {@link VariableHolder}.
	 */
	public Collection<VariableHolder> contributors();
	
}
