package org.openimaj.squall.compile.rif;

import java.util.List;

import org.openimaj.rif.RIFRuleSet;
import org.openimaj.squall.compile.jena.SourceRulePair;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.functions.rif.RIFExternalFunctionLibrary;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>, based on {@link SourceRulePair} by Sina Samangooei (ss@ecs.soton.ac.uk)
 * A pair representing source {@link Stream} of {@link Triple} and a {@link RIFRuleSet} against those triples
 *
 */
public class SourceRulesetLibsTrio {

	private List<ISource<Stream<Context>>> first;
	private RIFRuleSet second;
	private List<RIFExternalFunctionLibrary> third;
	
	/**
	 * @param sources
	 * @param rules
	 */
	public SourceRulesetLibsTrio(List<ISource<Stream<Context>>> sources, RIFRuleSet rules, List<RIFExternalFunctionLibrary> libs) {
		this.first = sources;
		this.second = rules;
		this.third = libs;
	}
	
	/**
	 * @return
	 */
	public List<ISource<Stream<Context>>> firstObject(){
		return this.first;
	}
	
	/**
	 * @return
	 */
	public RIFRuleSet secondObject(){
		return this.second;
	}
	
	/**
	 * @return
	 */
	public List<RIFExternalFunctionLibrary> thirdObject(){
		return this.third;
	}

}