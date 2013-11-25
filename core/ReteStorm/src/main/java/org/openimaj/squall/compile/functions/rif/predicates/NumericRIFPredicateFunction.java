package org.openimaj.squall.compile.functions.rif.predicates;

import java.util.Map;

import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public abstract class NumericRIFPredicateFunction extends BaseRIFPredicateFunction {

	/**
	 * @param ns
	 * @throws RIFPredicateException
	 */
	public NumericRIFPredicateFunction(Node[] ns) throws RIFPredicateException {
		super(ns);
	}
	
	protected Double extractBinding(Map<String, Node> binds, Node node) {
		try{
			return ((Number) super.extractBinding(binds, node)).doubleValue();
		}
		catch (ClassCastException e){
			throw new UnsupportedOperationException("Incorrect datatype for numeric comparison");
		}
	}

}
