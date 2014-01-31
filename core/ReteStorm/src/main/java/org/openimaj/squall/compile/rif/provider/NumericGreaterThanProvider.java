package org.openimaj.squall.compile.rif.provider;

import java.util.Map;

import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.compile.rif.provider.RIFExternalFunctionProvider.RuleWrappedPredicateFunction;
import org.openimaj.squall.compile.rif.provider.RIFExternalFunctionProvider.RuleWrappedValueFunction;
import org.openimaj.squall.data.RuleWrapped;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction;
import org.openimaj.squall.functions.rif.predicates.LiteralNotEqualFunction;
import org.openimaj.squall.functions.rif.predicates.NumericGreaterThanFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class NumericGreaterThanProvider extends RIFExternalFunctionProvider {

	/**
	 * @param reg
	 */
	public NumericGreaterThanProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}

	@Override
	public RuleWrappedNumericGreaterThanFunction apply(RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		try {
			IndependentPair<Node[], Map<Node, RuleWrappedValueFunction<?>>> data = extractNodesAndSubFunctions(atom);
			return new RuleWrappedNumericGreaterThanFunction(data.firstObject(), data.secondObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public RuleWrappedNumericGreaterThanFunction apply(RIFExternalValue in) {
		RIFAtom atom = in.getVal();
		try {
			IndependentPair<Node[], Map<Node, RuleWrappedValueFunction<?>>> data = extractNodesAndSubFunctions(atom);
			return new RuleWrappedNumericGreaterThanFunction(data.firstObject(), data.secondObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	protected static class RuleWrappedNumericGreaterThanFunction extends RuleWrappedPredicateFunction<NumericGreaterThanFunction> {

		public RuleWrappedNumericGreaterThanFunction(Node[] ns, Map<Node, RuleWrappedValueFunction<?>> funcMap) throws RIFPredicateException {
			super("NumericGreaterThan", ns, funcMap);
			this.wrap(new NumericGreaterThanFunction(ns, super.getRulelessFuncMap()));
		}
		
	}

}
