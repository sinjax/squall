package org.openimaj.squall.functions.rif.predicates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.functions.rif.calculators.BaseRIFValueFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import cern.colt.Arrays;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class NumericGreaterThanFunction extends NumericRIFPredicateFunction {

	private static final Logger logger = Logger.getLogger(NumericGreaterThanFunction.class);

	/**
	 * @param ns
	 * @throws RIFPredicateException
	 */
	public NumericGreaterThanFunction(Node[] ns, Map<Node, BaseRIFValueFunction> funcMap) throws RIFPredicateException {
		super(ns, funcMap);
	}
	
	@SuppressWarnings("javadoc") // required for kryo deserialisation by reflection
	public NumericGreaterThanFunction() throws RIFPredicateException {
		super(new Node[]{
				NodeFactory.createLiteral("foo"),
				NodeFactory.createVariable("bar")
				},
				new HashMap<Node, BaseRIFValueFunction>()
		);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7935891899097417140L;

	@Override
	public List<Context> applyRoot(Context in) {
		logger  .debug(String.format("Context(%s) sent to Predicate >%s" , in, Arrays.toString(super.nodes)));
		List<Context> ret = new ArrayList<Context>();
		Map<String,Node> binds = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		
		Double current = super.extractBinding(binds, super.nodes[0]);
		Double next = super.extractBinding(binds, super.nodes[1]);
		for (int i = 2; i < super.nodes.length; i++){
			if(current <= next) {
				logger  .debug(String.format("Numeric Greater Than check failed on comparison"));
				return ret;
			}
			current = next;
			next = super.extractBinding(binds, super.nodes[i]);
		}
		ret.add(in);
		return ret;
	}
	
}