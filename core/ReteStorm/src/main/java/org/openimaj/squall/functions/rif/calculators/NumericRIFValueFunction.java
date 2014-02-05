package org.openimaj.squall.functions.rif.calculators;

import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class NumericRIFValueFunction extends BaseValueFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5477404099397402846L;

	/**
	 * @param ns
	 * @param rn
	 * @param funcs 
	 * @throws RIFPredicateException
	 */
	public NumericRIFValueFunction(Node[] ns, Node_Variable rn, Map<Node, BaseValueFunction> funcs)
			throws RIFPredicateException {
		super(ns, rn, funcs);
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
