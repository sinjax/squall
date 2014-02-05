package org.openimaj.squall.compile.rif.provider.predicates;

import java.util.Map;

import org.openimaj.rifcore.conditions.formula.RIFEqual;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.rif.predicates.PredicateEqualityFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.rif.predicates.PredicateEqualityFunction.RuleWrappedEqualityFunction;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFEqualFunctionProvider extends RIFPredicateFunctionProvider<RIFEqual> {

	/**
	 * @param reg
	 */
	public RIFEqualFunctionProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}

	@Override
	public RuleWrappedEqualityFunction apply(RIFEqual in) {
		try {
			IndependentPair<Node[], Map<Node, RuleWrappedValueFunction<?>>> data = extractNodesAndSubFunctions(in);
			return PredicateEqualityFunction.ruleWrapped(data.firstObject(), data.secondObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
