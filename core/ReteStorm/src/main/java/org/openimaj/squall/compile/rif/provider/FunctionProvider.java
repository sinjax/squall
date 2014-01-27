package org.openimaj.squall.compile.rif.provider;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <IN>
 * @param <REG> 
 */
public abstract class FunctionProvider<IN,REG> implements Function<IN, IVFunction<Context, Context>> {

	private FunctionRegistry<REG> reg;
	
	/**
	 * @param reg
	 */
	public FunctionProvider (FunctionRegistry<REG> reg){
		this.reg = reg;
	}
	
	protected FunctionRegistry<REG> getRegistry(){
		return this.reg;
	}
	
}
