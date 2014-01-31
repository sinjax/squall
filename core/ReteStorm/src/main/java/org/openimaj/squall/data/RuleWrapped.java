package org.openimaj.squall.data;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public abstract class RuleWrapped<T> extends AnonimisedRuleVariableHolder {

	private T wrapped;
	
	protected void wrap(T toWrap){
		this.wrapped = toWrap;
	}
	
	/**
	 * @return
	 */
	public T getWrapped(){
		return this.wrapped;
	}
	
}
