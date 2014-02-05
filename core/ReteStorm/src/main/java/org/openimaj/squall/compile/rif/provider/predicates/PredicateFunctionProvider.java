package org.openimaj.squall.compile.rif.provider.predicates;

import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.compile.rif.provider.FunctionProvider;
import org.openimaj.squall.compile.rif.provider.FunctionRegistry;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;

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
