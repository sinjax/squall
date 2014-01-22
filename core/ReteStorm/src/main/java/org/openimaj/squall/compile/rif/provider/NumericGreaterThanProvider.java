package org.openimaj.squall.compile.rif.provider;

import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.functions.rif.predicates.NumericGreaterThanFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk), David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class NumericGreaterThanProvider extends ExternalFunctionProvider {

	@Override
	public IVFunction<Context, Context> apply(RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		try {
			return new NumericGreaterThanFunction(extractNodes(atom));
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public IVFunction<Context, Context> apply(RIFExternalValue in) {
		RIFAtom atom = in.getVal();
		try {
			return new NumericGreaterThanFunction(extractNodes(atom));
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
