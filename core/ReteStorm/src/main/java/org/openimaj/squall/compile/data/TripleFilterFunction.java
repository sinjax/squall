package org.openimaj.squall.compile.data;

import java.util.Map;

import org.openimaj.squall.data.ComponentInformation;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Filter a triple, return bindings against variables
 *
 */
public class TripleFilterFunction implements ComponentInformationFunction<Triple, Map<String, String>> {
	/**
	 * @param clause construct using a {@link TriplePattern}
	 */
	public TripleFilterFunction(TriplePattern clause) {
	}

	@Override
	public Map<String, String> apply(Triple in) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ComponentInformation information() {
		// TODO Auto-generated method stub
		return null;
	}
}