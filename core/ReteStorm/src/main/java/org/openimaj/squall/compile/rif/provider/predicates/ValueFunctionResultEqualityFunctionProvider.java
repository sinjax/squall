package org.openimaj.squall.compile.rif.provider.predicates;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.squall.compile.rif.provider.FunctionProvider;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.rif.predicates.PredicateEqualityFunction;
import org.openimaj.squall.functions.rif.predicates.PredicateEqualityFunction.RuleWrappedEqualityFunction;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class ValueFunctionResultEqualityFunctionProvider implements
		FunctionProvider<RuleWrappedValueFunction<?>> {

	private Node_Variable var;
	
	/**
	 * @param var
	 */
	public ValueFunctionResultEqualityFunctionProvider(Node_Variable var){
		this.var = var;
	}
	
	@Override
	public RuleWrappedEqualityFunction apply(RuleWrappedValueFunction<?> in) {
		Map<Node, RuleWrappedValueFunction<?>> funcs = new HashMap<Node, RuleWrappedValueFunction<?>>();
		Node[] nodes = new Node[]{
			this.var,
			in.getWrapped().getResultVarNode()
		};
		try {
			return PredicateEqualityFunction.ruleWrapped(nodes, funcs);
		} catch (RIFPredicateException e) {
			throw new RuntimeException(e);
		}
	}

}
