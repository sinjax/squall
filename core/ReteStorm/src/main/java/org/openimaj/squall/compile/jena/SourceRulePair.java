package org.openimaj.squall.compile.jena;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.data.ISource;
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
public class SourceRulePair extends IndependentPair<List<ISource<Stream<Context>>>, List<Rule>>{

	/**
	 * @param sources
	 * @param rules
	 */
	public SourceRulePair(List<ISource<Stream<Context>>> sources, List<Rule> rules) {
		super(sources, rules);
	}

	/**
	 * A simplified stream which needs no initialisation and rules
	 * 
	 * @param stream
	 * @param rules
	 * @return the pair
	 */
	public static SourceRulePair simplePair(ISource<Stream<Context>> stream, List<Rule> rules) {
		List<ISource<Stream<Context>>> sources = new ArrayList<>();
		sources.add(
			stream
		);
		return new SourceRulePair(sources, rules);
	}

	

}
