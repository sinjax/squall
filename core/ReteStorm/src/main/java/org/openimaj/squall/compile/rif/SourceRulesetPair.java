package org.openimaj.squall.compile.rif;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.rif.RIFRuleSet;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.IStreamWrapper;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.compile.jena.SourceRulePair;
import org.openimaj.util.data.Context;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>, based on {@link SourceRulePair} by Sina Samangooei (ss@ecs.soton.ac.uk)
 * A pair representing source {@link Stream} of {@link Triple} and a {@link RIFRuleSet} against those triples
 *
 */
public class SourceRulesetPair extends IndependentPair<List<IStream<Context>>, RIFRuleSet>{

	/**
	 * @param sources
	 * @param rules
	 */
	public SourceRulesetPair(List<IStream<Context>> sources, RIFRuleSet rules) {
		super(sources, rules);
	}

	/**
	 * A simplified stream which needs no initialisation and rules
	 * 
	 * @param stream
	 * @param rules
	 * @return the pair
	 */
	public static SourceRulesetPair simplePair(Stream<Context> stream, RIFRuleSet rules) {
		List<IStream<Context>> sources = new ArrayList<IStream<Context>>();
		Initialisable init = new Initialisable() {
			
			@Override
			public void setup() { }
			
			@Override
			public void cleanup() { }
		};
		sources.add(
			new IStreamWrapper<Context>(
				stream
			,init)
		);
		return new SourceRulesetPair(sources, rules);
	}

	

}