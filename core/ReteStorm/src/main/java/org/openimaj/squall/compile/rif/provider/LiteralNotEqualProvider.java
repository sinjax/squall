package org.openimaj.squall.compile.rif.provider;

import org.apache.log4j.Logger;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.rif.predicates.LiteralNotEqualFunction;
import org.openimaj.util.data.Context;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class LiteralNotEqualProvider extends ExternalFunctionProvider {

	private static final Logger logger = Logger.getLogger(LiteralNotEqualProvider.class);
	
	@Override
	public IVFunction<Context, Context> apply(RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		try {
			return new LiteralNotEqualFunction(extractNodes(atom));
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public IVFunction<Context, Context> apply(RIFExternalValue in) {
		RIFAtom atom = in.getVal();
		try {
			return new LiteralNotEqualFunction(extractNodes(atom));
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
