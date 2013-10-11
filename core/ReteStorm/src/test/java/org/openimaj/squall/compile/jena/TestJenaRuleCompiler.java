package org.openimaj.squall.compile.jena;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.rdf.storm.topology.ReteTopologyTest;
import org.openimaj.squall.build.OIStreamBuilder;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestJenaRuleCompiler {
	
	private SourceRulePair sourceRules;
	
	
	@org.junit.Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private File output;
	private File input;
	
	/**
	 * @throws IOException 
	 * 
	 */
	@Before
	public void before() throws IOException{
		InputStream nTripleStream = ReteTopologyTest.class.getResourceAsStream("/test.rdfs");
		InputStream ruleStream = TestJenaRuleCompiler.class.getResourceAsStream("/test.single.rules");
		
		Stream<Context> tripleContextStream = 
			new CollectionStream<Triple>(JenaUtils.readNTriples(nTripleStream))
			.map(new ContextWrapper("triple")
		);
		
		List<Rule> rules = JenaUtils.readRules(ruleStream);
		sourceRules = SourceRulePair.simplePair(tripleContextStream,rules);
		
	}

	
	
	/**
	 * 
	 */
	@Test
	public void testCompiler(){
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		ContextCPS comp = jrc.compile(sourceRules);
		GreedyOrchestrator go = new GreedyOrchestrator();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp);
		OIStreamBuilder builder = new OIStreamBuilder();
		builder.build(orchestrated);
	}

}
