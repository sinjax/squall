package org.openimaj.squall.compile.jena;

import java.util.List;

import org.openimaj.squall.compile.data.IStream;
import org.openimaj.util.data.Context;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * A pair representing source {@link Stream} of {@link Triple} and a {@link List} of {@link Rule} against those triples
 *
 */
public class SourceRulePair extends IndependentPair<List<IStream<Context>>, List<Rule>>{

	/**
	 * @param sources
	 * @param rules
	 */
	public SourceRulePair(List<IStream<Context>> sources, List<Rule> rules) {
		super(sources, rules);
	}

	

}
