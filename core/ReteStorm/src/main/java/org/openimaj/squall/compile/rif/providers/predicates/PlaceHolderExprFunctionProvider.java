package org.openimaj.squall.compile.rif.providers.predicates;

import java.util.Map;

import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.predicates.PlaceHolderPredicateFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class PlaceHolderExprFunctionProvider extends RIFPredicateFunctionProvider<RIFExpr>{
	/**
	 * @param reg
	 */
	public PlaceHolderExprFunctionProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}
	
	@Override
	public RuleWrappedPredicateFunction<?> apply(RIFExpr in) {
		RIFAtom atom = in.getCommand();
		Node_Concrete op = atom.getOp().getNode();
		String name = op.isLiteral()
						? op.getLiteralValue().toString()
						: op.isURI()
							? op.getURI()
							: "_:";
		try {
			IndependentPair<Node[], Map<Node, RuleWrappedValueFunction<?>>> data = extractNodesAndSubFunctions(atom);
			return PlaceHolderPredicateFunction.ruleWrapped(name, data.firstObject(), data.secondObject());
		} catch (RIFPredicateException e) {
			throw new RuntimeException(e);
		}
	}

}