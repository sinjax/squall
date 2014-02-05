package org.openimaj.squall.functions.rif.predicates;

import java.util.Map;

import org.openimaj.squall.functions.rif.calculators.BaseValueFunction;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public abstract class NumericPredicateFunction extends BasePredicateFunction {

	/**
	 * @param ns
	 * @param funcMap 
	 * @throws RIFPredicateException
	 */
	public NumericPredicateFunction(Node[] ns, Map<Node, BaseValueFunction> funcMap) throws RIFPredicateException {
		super(ns, funcMap);
	}
	
	@Override
	protected Double extractBinding(Map<String, Node> binds, int nodeIndex) {
		try{
			return ((Number) super.extractBinding(binds, nodeIndex)).doubleValue();
		}
		catch (ClassCastException e){
			throw new UnsupportedOperationException("Incorrect datatype for numeric comparison");
		}
	}

}
