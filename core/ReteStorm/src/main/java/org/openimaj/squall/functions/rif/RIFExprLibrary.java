package org.openimaj.squall.functions.rif;

import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public interface RIFExprLibrary {
	
	/**
	 * Takes a RIFExpr object and returns the corresponding IVFunction.
	 * @param expr
	 * @return
	 * 		The IVFunction corresponding to the provided command.
	 * @throws UnsupportedOperationException
	 * 		Throws an exception if the function specified is not a recognised Expr of RIF-Core.
	 */
	public BaseRIFPredicateFunction compile(RIFExpr expr) throws UnsupportedOperationException;

}
