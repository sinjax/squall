package org.openimaj.squall.compile.rif.provider;

import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
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
	public IVFunction<Context, Context> apply(RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		try {
			IndependentPair<Node[], IVFunction<Context,Context>[]> data = extractNodesAndSubFunctions(atom);
			return new NumericGreaterThanFunction(data.firstObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public IVFunction<Context, Context> apply(RIFExternalValue in) {
		RIFAtom atom = in.getVal();
		try {
			IndependentPair<Node[], IVFunction<Context,Context>[]> data = extractNodesAndSubFunctions(atom);
			return new NumericGreaterThanFunction(data.firstObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
