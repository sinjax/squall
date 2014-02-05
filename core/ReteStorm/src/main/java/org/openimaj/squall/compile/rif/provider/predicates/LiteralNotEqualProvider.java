package org.openimaj.squall.compile.rif.provider.predicates;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.rif.predicates.LiteralNotEqualFunction;
import org.openimaj.squall.functions.rif.predicates.LiteralNotEqualFunction.RuleWrappedLiteralNotEqualFunction;
import org.openimaj.util.pair.IndependentPair;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class LiteralNotEqualProvider extends RIFExternalFunctionProvider {

	private static final Logger logger = Logger.getLogger(LiteralNotEqualProvider.class);
	
	/**
	 * @param reg
	 */
	public LiteralNotEqualProvider(RIFExprFunctionRegistry reg) {
		super(reg);
	}

	@Override
	public RuleWrappedValueFunction<? extends BaseValueFunction> apply(RIFExternalExpr in) {
		Node opNode = in.getExpr().getCommand().getOp().getNode();
		throw new UnsupportedOperationException(String.format("Cannot use the filtering predicate %s to supply a value.",
																opNode.isLiteral()
																	? opNode.getLiteralValue().toString()
																	: opNode.getURI()));
	}

	@Override
	public RuleWrappedLiteralNotEqualFunction apply(RIFExternalValue in) {
		RIFAtom atom = in.getVal();
		try {
			IndependentPair<Node[], Map<Node, RuleWrappedValueFunction<?>>> data = extractNodesAndSubFunctions(atom);
			return LiteralNotEqualFunction.ruleWrapped(data.firstObject(), data.secondObject());
		} catch (RIFPredicateException e) {
			throw new UnsupportedOperationException(e);
		}
	}

}
