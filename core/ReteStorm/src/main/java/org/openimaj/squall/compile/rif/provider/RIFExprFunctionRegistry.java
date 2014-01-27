package org.openimaj.squall.compile.rif.provider;

import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class RIFExprFunctionRegistry extends FunctionRegistry<RIFExpr> {

	@Override
	public IVFunction<Context,Context> compile(RIFExpr in) {
		Node node = in.getCommand().getOp().getNode();
		String funcName = node.isLiteral()
							? node.getLiteralValue().toString()
							: node.getURI();
		return super.compile(funcName).apply(in);
	}

}
