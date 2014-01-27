package org.openimaj.squall.compile.rif.provider;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.rifcore.conditions.RIFExternal;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFDatum;
import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;

/**
 * A function which given a {@link RIFExternal} can provide a working implementation
 * of that function.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class RIFExternalFunctionProvider extends FunctionProvider<RIFExternal,RIFExpr> {
	
	/**
	 * @param reg
	 */
	public RIFExternalFunctionProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}
	
	@Override
	public IVFunction<Context, Context> apply(RIFExternal in) {
		if(in instanceof RIFExternalExpr) return apply((RIFExternalExpr)in);
		else return apply((RIFExternalValue)in);
	}
	/**
	 * @param in
	 * @return
	 */
	public abstract IVFunction<Context, Context> apply(RIFExternalExpr in) ;
	
	/**
	 * @param in
	 * @return
	 */
	public abstract IVFunction<Context, Context> apply(RIFExternalValue in) ;
	
	protected IndependentPair<Node[],IVFunction<Context,Context>[]> extractNodesAndSubFunctions(RIFAtom atom) {
		List<Node> nodes = new ArrayList<Node>();
		List<IVFunction<Context,Context>> funcs = new ArrayList<IVFunction<Context,Context>>();
		for (RIFDatum node : atom) {
			nodes.add(node.getNode());
			if (node instanceof RIFExpr){
				if (node instanceof RIFExternal){
					RIFExternal exp = (RIFExternal) node;
					funcs.add(RIFExternalFunctionRegistry.compile(exp));
				} else {
					RIFExpr exp = (RIFExpr) node;
					funcs.add(this.getRegistry().compile(exp));
				}
			}
		}
		
		Node[] nodeArr = (Node[]) nodes.toArray(new Node[0]);
		@SuppressWarnings("unchecked")
		IVFunction<Context, Context>[] funcArr = (IVFunction<Context, Context>[]) funcs.toArray(new IVFunction[0]);
		
		return new IndependentPair<Node[], IVFunction<Context,Context>[]>(nodeArr,funcArr);
	}
	
}
