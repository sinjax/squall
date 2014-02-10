package org.openimaj.squall.functions.predicates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.functions.calculators.BaseValueFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import cern.colt.Arrays;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class NumericGreaterThanFunction extends NumericPredicateFunction {

	private static final Logger logger = Logger.getLogger(NumericGreaterThanFunction.class);
	
	/**
	 * @param ns
	 * @param funcMap
	 * @return
	 * @throws RIFPredicateException
	 */
	public static RuleWrappedNumericGreaterThanFunction ruleWrapped(
															Node[] ns,
															Map<Node, RuleWrappedValueFunction<?>> funcMap
														) throws RIFPredicateException {
		return new RuleWrappedNumericGreaterThanFunction(ns, funcMap);
	}

	/**
	 * @param ns
	 * @throws RIFPredicateException
	 */
	public NumericGreaterThanFunction(Node[] ns, Map<Node, BaseValueFunction> funcMap) throws RIFPredicateException {
		super(ns, funcMap);
	}
	
	@SuppressWarnings("javadoc") // required for kryo deserialisation by reflection
	public NumericGreaterThanFunction() throws RIFPredicateException {
		super(new Node[]{
				NodeFactory.createLiteral("foo"),
				NodeFactory.createVariable("bar")
				},
				new HashMap<Node, BaseValueFunction>()
		);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7935891899097417140L;

	@Override
	public List<Context> applyRoot(Context in) {
		logger  .debug(String.format("Context(%s) sent to Predicate >%s" , in, Arrays.toString(super.getNodes())));
		List<Context> ret = new ArrayList<Context>();
		Map<String,Node> binds = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		
		Double current = super.extractBinding(binds, 0);
		for (int i = 1; i < super.getNodeCount(); i++){
			Double next = super.extractBinding(binds, i);
			if(current <= next) {
				logger  .debug(String.format("Numeric Greater Than check failed on comparison"));
				return ret;
			}
			current = next;
		}
		ret.add(in);
		return ret;
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static class RuleWrappedNumericGreaterThanFunction extends RuleWrappedPredicateFunction<NumericGreaterThanFunction> {

		protected RuleWrappedNumericGreaterThanFunction(Node[] ns, Map<Node, RuleWrappedValueFunction<?>> funcMap) throws RIFPredicateException {
			super("NumericGreaterThan", ns, funcMap);
			this.wrap(new NumericGreaterThanFunction(ns, super.getRulelessFuncMap()));
		}
		
	}
	
}