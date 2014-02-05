package org.openimaj.squall.compile.rif.provider.consequences;

import org.openimaj.squall.compile.rif.provider.FunctionProvider;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <IN>
 */
public abstract class BaseConsequenceProvider<IN> implements FunctionProvider<IN> {
	
	private String ruleID;
	
	/**
	 * @param rID
	 */
	public BaseConsequenceProvider(String rID){
		this.ruleID = rID;
	}
	
	protected String getRuleID(){
		return this.ruleID;
	}

}
