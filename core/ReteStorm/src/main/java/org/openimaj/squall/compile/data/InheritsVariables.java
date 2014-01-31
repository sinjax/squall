package org.openimaj.squall.compile.data;

import java.util.Map;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public interface InheritsVariables {
	
	/**
	 * @return true if the source variable template has already been set.
	 */
	public boolean areSourceVariablesSet();
	
	/**
	 * @return the identifier of the source variable holder, or "No Source" if the source variables have not been set.
	 */
	public String getSourceVarHolderIdent();
	
	/**
	 * @param varMap - the map of variables to use to the base variables of the object inheriting variables
	 * @return the identifier of the source variable holder, or "No Source" if the source variables have not been set.
	 */
	public String getSourceVarHolderIdent(Map<String, String> varMap);
	
	/**
	 * @param arvh
	 * @return whether the arvh could be inherited from.
	 */
	public boolean setSourceVariables(AnonimisedRuleVariableHolder arvh);
	
}
