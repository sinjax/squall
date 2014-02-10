package org.openimaj.squall.providers.predicates;

import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;
import org.openimaj.squall.providers.FunctionProvider;
import org.openimaj.squall.providers.FunctionRegistry;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <IN>
 * @param <REG>
 */
public abstract class PredicateFunctionProvider<IN, REG> implements FunctionProvider<IN> {

	private FunctionRegistry<REG> reg;
	
	/**
	 * @param reg
	 */
	public PredicateFunctionProvider(FunctionRegistry<REG> reg){
		this.reg = reg;
	}
	
	protected RuleWrappedFunction<?> compileFromRegistry(REG in){
		return this.reg.compile(in);
	}

	@Override
	public abstract RuleWrappedPredicateFunction<?> apply(IN in);
	
}
