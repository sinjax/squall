package org.openimaj.squall.compile.rif.provider.predicates;

import org.openimaj.rifcore.conditions.RIFExternal;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;

/**
 * A function which given a {@link RIFExternal} can provide a working implementation
 * of that function.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class RIFExternalFunctionProvider extends RIFPredicateFunctionProvider<RIFExternal> {
	
	/**
	 * @param reg
	 */
	public RIFExternalFunctionProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}
	
	@Override
	public RuleWrappedPredicateFunction<? extends BasePredicateFunction> apply(RIFExternal in) {
		if(in instanceof RIFExternalExpr) return apply((RIFExternalExpr)in);
		else return apply((RIFExternalValue)in);
	}
	/**
	 * @param in
	 * @return
	 */
	public abstract RuleWrappedValueFunction<? extends BaseValueFunction> apply(RIFExternalExpr in) ;
	
	/**
	 * @param in
	 * @return
	 */
	public abstract RuleWrappedPredicateFunction<? extends BasePredicateFunction> apply(RIFExternalValue in) ;

}
