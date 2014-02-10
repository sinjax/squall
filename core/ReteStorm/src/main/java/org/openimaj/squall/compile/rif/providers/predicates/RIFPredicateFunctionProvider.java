package org.openimaj.squall.compile.rif.providers.predicates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rifcore.conditions.RIFExternal;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFDatum;
import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.data.RIFFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.providers.predicates.PredicateFunctionProvider;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <IN>
 */
public abstract class RIFPredicateFunctionProvider<IN> extends
		PredicateFunctionProvider<IN, RIFExpr> {

	/**
	 * @param reg
	 */
	public RIFPredicateFunctionProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}
	
	protected IndependentPair<Node[],Map<Node, RuleWrappedValueFunction<?>>> extractNodesAndSubFunctions(Iterable<RIFDatum> atom) {
		List<Node> nodes = new ArrayList<Node>();
		Map<Node, RuleWrappedValueFunction<?>> funcMap = new HashMap<Node, RuleWrappedValueFunction<?>>();
		for (RIFDatum node : atom) {
			nodes.add(node.getNode());
			if (node instanceof RIFFunction){
				RuleWrappedValueFunction<?> varValFunc;
				if (node instanceof RIFExternalExpr){
					RIFExternalExpr exp = (RIFExternalExpr) node;
					varValFunc = RIFExternalFunctionRegistry.compile(exp);
				} else if (node instanceof RIFExpr) {
					RIFExpr exp = (RIFExpr) node;
					varValFunc = (RuleWrappedValueFunction<?>) compileFromRegistry(exp);
				} else {
					throw new UnsupportedOperationException(String.format("Unrecognised extension of RIFFunction: %s", node.toString()));
				}
				funcMap.put(node.getNode(), varValFunc);
			}
		}
		
		Node[] nodeArr = (Node[]) nodes.toArray(new Node[0]);
		
		return new IndependentPair<Node[], Map<Node,RuleWrappedValueFunction<?>>>(nodeArr,funcMap);
	}

}
