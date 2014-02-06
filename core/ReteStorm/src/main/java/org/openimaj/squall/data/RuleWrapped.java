package org.openimaj.squall.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public class RuleWrapped<T> extends AnonimisedRuleVariableHolder {

	private T wrapped;
	private AnonimisedRuleVariableHolder arvh;
	
	/**
	 * @param arvh
	 */
	public RuleWrapped(AnonimisedRuleVariableHolder arvh){
		this.arvh = arvh;
	}
	
	/**
	 * @param arvh
	 * @param toWrap
	 */
	public RuleWrapped(AnonimisedRuleVariableHolder arvh, T toWrap){
		this(arvh);
		this.wrap(toWrap);
	}
	
	protected void wrap(T toWrap){
		this.wrapped = toWrap;
	}
	
	/**
	 * @return
	 */
	public T getWrapped(){
		return this.wrapped;
	}
	
	/**
	 * @return
	 */
	public AnonimisedRuleVariableHolder getVariableHolder(){
		return this.arvh;
	}
	
	@Override
	public boolean addVariable(String name) {
		return this.arvh.addVariable(name);
	}
	
	@Override
	public List<String> ruleVariables() {
		return this.arvh.ruleVariables();
	}
	
	@Override
	public Map<String, String> ruleToBaseVarMap() {
		return this.arvh.ruleToBaseVarMap();
	}
	
	@Override
	public boolean resetVars() {
		return this.arvh.resetVars();
	}
	
	@Override
	public Collection<AnonimisedRuleVariableHolder> contributors() {
		return this.arvh.contributors();
	}
	
	@Override
	public String identifier() {
		return this.arvh.identifier();
	}
	
	@Override
	public String identifier(Map<String, String> varmap) {
		return this.arvh.identifier(varmap);
	}
	
	@Override
	public String toString(){
		return this.arvh.toString();
	}
	
}
