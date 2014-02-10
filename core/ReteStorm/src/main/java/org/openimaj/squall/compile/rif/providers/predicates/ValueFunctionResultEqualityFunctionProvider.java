package org.openimaj.squall.compile.rif.providers.predicates;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.predicates.PredicateEqualityFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.predicates.PredicateEqualityFunction.RuleWrappedEqualityFunction;
import org.openimaj.squall.providers.FunctionProvider;

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
