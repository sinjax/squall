package org.openimaj.squall.revised.compile.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public interface AnonimisedRuleVariableHolder extends VariableHolder {

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
	 * @param toMirror 
	 * @return 
	 */
	public boolean mirrorInRule(AnonimisedRuleVariableHolder toMirror);
	
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
	public Collection<AnonimisedRuleVariableHolder> contributors();
	
}
