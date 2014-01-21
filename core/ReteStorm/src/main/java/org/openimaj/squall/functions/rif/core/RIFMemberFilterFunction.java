package org.openimaj.squall.functions.rif.core;

import org.openimaj.rifcore.conditions.formula.RIFMember;
import org.openimaj.squall.functions.rif.filters.BaseTripleFilterFunction;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFMemberFilterFunction extends BaseTripleFilterFunction {
	
	/**
	 * The URI used for the predicate denoting that a Subject is of rdf:type Object.
	 */
	public static final String RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	private static TriplePattern extractTriplePattern(RIFMember rm){
		TriplePattern tp = new TriplePattern(
				rm.getInstance().getNode(),
				NodeFactory.createURI(RDF_TYPE_URI),
				rm.getInClass().getNode()
			);
		return tp; //TODO proper conversion
	}
	
	/**
	 * @param rm
	 * 			The {@link RIFMember} clause to convert to a filter function.
	 */
	public RIFMemberFilterFunction(RIFMember rm) {
		super(extractTriplePattern(rm));
	}

}
