package org.openimaj.squall.compile.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestJenaRuleCompiler {
	
	List<Rule> rules;
	private SourceRulePair sourceRules;
	private List<Stream<Triple>> sources;
	private Collection<Triple> tripleCol;
	
	/**
	 * 
	 */
	@Before
	public void before(){
		this.rules = JenaUtils.readRules(TestJenaRuleCompiler.class.getResourceAsStream("/test.rules"));
		sources = new ArrayList<Stream<Triple>>();
		sources.add(new CollectionStream<Triple>(tripleCol));
		sourceRules = new SourceRulePair(sources, rules);
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testCompiler(){
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		jrc.compile(sourceRules);
	}

}
