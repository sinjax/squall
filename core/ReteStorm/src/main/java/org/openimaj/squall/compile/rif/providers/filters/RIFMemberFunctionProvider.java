package org.openimaj.squall.compile.rif.providers.filters;

import org.openimaj.rifcore.conditions.formula.RIFMember;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.functions.filters.TripleFilterFunction;
import org.openimaj.squall.providers.FunctionProvider;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFMemberFunctionProvider implements FunctionProvider<RIFMember> {

	/**
	 * The URI used for the predicate denoting that a Subject is of rdf:type Object.
	 */
	public static final String RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	@Override
	public RuleWrappedFunction<TripleFilterFunction> apply(RIFMember in) {
		TriplePattern tp = new TriplePattern(
				in.getInstance().getNode(),
				NodeFactory.createURI(RDF_TYPE_URI),
				in.getInClass().getNode()
			);
		return TripleFilterFunction.ruleWrapped(tp);
	}

}
