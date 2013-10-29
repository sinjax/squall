package org.openimaj.squall.functions.rif.filters;

import org.openimaj.rif.conditions.formula.RIFMember;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFMemberFilterFunction extends BaseTripleFilterFunction {

	private static TriplePattern extractTriplePattern(RIFMember rm){
		TriplePattern tp = new TriplePattern(
				rm.getInstance().getNode(),
				Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				rm.getInClass().getNode()
			);
		return tp; //TODO proper converstion
	}
	
	/**
	 * @param rm
	 * 			The {@link RIFMember} clause to convert to a filter function.
	 */
	public RIFMemberFilterFunction(RIFMember rm) {
		super(extractTriplePattern(rm));
	}

}
