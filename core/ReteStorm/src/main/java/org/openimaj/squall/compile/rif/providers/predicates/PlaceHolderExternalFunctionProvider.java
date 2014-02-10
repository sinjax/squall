package org.openimaj.squall.compile.rif.providers.predicates;

import java.util.Map;

import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.functions.calculators.BaseValueFunction;
import org.openimaj.squall.functions.calculators.PlaceHolderValueFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction;
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
public class PlaceHolderExternalFunctionProvider extends RIFExternalFunctionProvider{
	/**
	 * @param reg
	 */
	public PlaceHolderExternalFunctionProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}
	
	@Override
	public RuleWrappedValueFunction<? extends BaseValueFunction> apply(
			RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		Node_Concrete op = atom.getOp().getNode();
		String name = op.isLiteral()
						? op.getLiteralValue().toString()
						: op.isURI()
							? op.getURI()
							: "_:";
		try {
			IndependentPair<Node[], Map<Node, RuleWrappedValueFunction<?>>> data = extractNodesAndSubFunctions(atom);
			return PlaceHolderValueFunction.ruleWrapped(name, data.firstObject(), in.getNode(), data.secondObject());
		} catch (RIFPredicateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public RuleWrappedPredicateFunction<? extends BasePredicateFunction> apply(
			RIFExternalValue in) {
		RIFAtom atom = in.getVal();
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