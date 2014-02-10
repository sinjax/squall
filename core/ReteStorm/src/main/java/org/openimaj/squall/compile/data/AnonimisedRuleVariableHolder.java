package org.openimaj.squall.compile.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class AnonimisedRuleVariableHolder extends VariableHolder {

	private Map<String, String> ruleToVarMap;

	/**
	 * 
	 */
	public AnonimisedRuleVariableHolder(){
		super();
		this.ruleToVarMap = new HashMap<String, String>();
	}
	
	@Override
	public AnonimisedRuleVariableHolder clone() throws CloneNotSupportedException {
		AnonimisedRuleVariableHolder clone = (AnonimisedRuleVariableHolder) super.clone();
		Map<String, String> map = new HashMap<String, String>();
		for (String key : clone.ruleToVarMap.keySet()){
			map.put(key, clone.ruleToVarMap.get(key));
		}
		clone.ruleToVarMap = map;
		return clone;
	}
	
	/**
	 * @return the variables used in the current rule's use of this function, ordered by appearance in the function output.
	 */
	public List<String> ruleVariables(){
		Map<String, String> baseToRuleVars = new HashMap<String, String>();
		for (String ruleVar : this.ruleToVarMap.keySet()){
			baseToRuleVars.put(this.ruleToVarMap.get(ruleVar), ruleVar);
		}
		
		List<String> rvars = new ArrayList<String>();
		for (String baseVar : this.variables()){
			rvars.add(baseToRuleVars.get(baseVar));
		}
		return rvars;
	}
	
	/**
	 * @return
	 * 		A mapping between the variables used in a given rule's use of this {@link VariableHolder} and their equivalent
	 * 		underlying variables.
	 */
	public Map<String, String> ruleToBaseVarMap(){
		return Collections.unmodifiableMap(this.ruleToVarMap);
	}
	
	/**
	 * @param key
	 * @param value
	 * @return
	 */
	protected String putRuleToBaseVarMapEntry(String key, String value){
		return this.ruleToVarMap.put(key, value);
	}
	
	/**
	 * @param key
	 * @param value
	 * @return
	 */
	protected String getBaseFromRuleVar(String key){
		return this.ruleToVarMap.get(key);
	}
	
	/**
	 * Produces an representation of this function such that all variables are
	 * replaced with the string dictated by the variable map in varmap.
	 * @param varmap
	 * 		Map of underlying variable names for this {@link VariableHolder} mapped to the desired variable names.
	 * @return anonimised name
	 */
	public abstract String identifier(Map<String, String> varmap);
	
	/**
	 * @return The collection of atomic {@link VariableHolder}s that contribute to this {@link VariableHolder}.
	 */
	public Collection<AnonimisedRuleVariableHolder> contributors() {
		Collection<AnonimisedRuleVariableHolder> conts = new ArrayList<AnonimisedRuleVariableHolder>();
		conts.add(this);
		return conts;
	}
	
	public boolean resetVars(){
		this.ruleToVarMap.clear();
		return super.resetVars();
	}
	
	public boolean wipeVars(){
		this.ruleToVarMap = null;
		return super.wipeVars();
	}
	
}
