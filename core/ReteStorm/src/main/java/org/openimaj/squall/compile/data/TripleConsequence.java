package org.openimaj.squall.compile.data;

import java.util.Map;

import org.openimaj.util.function.Function;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TripleConsequence implements Function<Map<String, String>, Triple> {

	private TriplePattern clause;

	/**
	 * @param clause
	 */
	public TripleConsequence(TriplePattern clause) {
		this.clause = clause;
	}

	@Override
	public Triple apply(Map<String, String> in) {
		return null;
	}

}
