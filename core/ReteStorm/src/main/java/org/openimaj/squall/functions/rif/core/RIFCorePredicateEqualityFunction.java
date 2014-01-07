package org.openimaj.squall.functions.rif.core;

import org.openimaj.rifcore.conditions.formula.RIFEqual;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateEqualityFunction;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFCorePredicateEqualityFunction extends BaseRIFPredicateEqualityFunction {

	private static Node[] extractEqualNodes(RIFEqual re){
		Node[] ret = new Node[2];
		ret[0] = re.getLeft().getNode();
		ret[1] = re.getRight().getNode();
		return ret;
	}
	
	/**
	 * @param re
	 * @throws RIFPredicateException
	 */
	public RIFCorePredicateEqualityFunction(RIFEqual re) throws RIFPredicateException {
		super(
			RIFCorePredicateEqualityFunction.extractEqualNodes(
				re
			)
		);
	}

}
