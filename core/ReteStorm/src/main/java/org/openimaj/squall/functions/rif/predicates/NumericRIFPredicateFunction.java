package org.openimaj.squall.functions.rif.predicates;

import java.util.Map;

import org.openimaj.squall.functions.rif.calculators.BaseRIFValueFunction;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public abstract class NumericRIFPredicateFunction extends BaseRIFPredicateFunction {

	/**
	 * @param ns
	 * @param funcMap 
	 * @throws RIFPredicateException
	 */
	public NumericRIFPredicateFunction(Node[] ns, Map<Node, BaseRIFValueFunction> funcMap) throws RIFPredicateException {
		super(ns, funcMap);
	}
	
	@Override
	protected Double extractBinding(Map<String, Node> binds, Node node) {
		try{
			return ((Number) super.extractBinding(binds, node)).doubleValue();
		}
		catch (ClassCastException e){
			throw new UnsupportedOperationException("Incorrect datatype for numeric comparison");
		}
	}

}
