package org.openimaj.squall.functions.rif.core;

import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.squall.functions.rif.RIFExprLibrary;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFCoreExprLibrary implements RIFExprLibrary {
	
	@Override
	public BaseRIFPredicateFunction compile(RIFExpr expr) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("RIF-Core translator: There are no builtin Exprs in RIF-Core.");
	}

}
