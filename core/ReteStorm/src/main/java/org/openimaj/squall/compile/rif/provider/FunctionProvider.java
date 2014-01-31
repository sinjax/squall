package org.openimaj.squall.compile.rif.provider;

import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.util.function.Function;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <IN>
 * @param <REG> 
 */
public abstract class FunctionProvider<IN,REG> implements Function<IN, RuleWrappedFunction<?>> {

	private FunctionRegistry<REG> reg;
	
	/**
	 * @param reg
	 */
	public FunctionProvider (FunctionRegistry<REG> reg){
		this.reg = reg;
	}
	
	protected RuleWrappedFunction<?> compileFromRegistry(REG in){
		return this.reg.compile(in);
	}
	
}
