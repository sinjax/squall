package org.openimaj.squall.compile.rif.providers.predicates;

import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.providers.FunctionRegistry;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class RIFExprFunctionRegistry extends FunctionRegistry<RIFExpr> {

	@Override
	public RuleWrappedValueFunction<?> compile(RIFExpr in) {
		Node node = in.getCommand().getOp().getNode();
		String funcName = node.isLiteral()
							? node.getLiteralValue().toString()
							: node.getURI();
		return (RuleWrappedValueFunction<?>) super.compile(funcName).apply(in);
	}

}
