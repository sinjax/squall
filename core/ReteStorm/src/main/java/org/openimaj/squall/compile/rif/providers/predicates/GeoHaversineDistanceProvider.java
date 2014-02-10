package org.openimaj.squall.compile.rif.providers.predicates;

import java.util.Map;

import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.calculators.geo.GeoHaversineDistanceFunction;
import org.openimaj.squall.functions.calculators.geo.GeoHaversineDistanceFunction.RuleWrappedGeoHaversineDistanceFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class GeoHaversineDistanceProvider extends RIFExternalFunctionProvider {

	/**
	 * @param reg
	 */
	public GeoHaversineDistanceProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}

	@Override
	public RuleWrappedGeoHaversineDistanceFunction apply(RIFExternalExpr in) {
		RIFAtom atom = in.getExpr().getCommand();
		Node_Variable rn = in.getNode();
		try {
			IndependentPair<Node[], Map<Node, RuleWrappedValueFunction<?>>> data = extractNodesAndSubFunctions(atom);
			return GeoHaversineDistanceFunction.ruleWrapped(data.firstObject(), rn, data.secondObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	public RuleWrappedPredicateFunction<? extends BasePredicateFunction> apply(RIFExternalValue in) {
		Node opNode = in.getVal().getOp().getNode();
		throw new UnsupportedOperationException(String.format("Cannot use the function %s to supply a predicate check.",
																opNode.isLiteral()
																	? opNode.getLiteralValue().toString()
																	: opNode.getURI()));
	}

}
