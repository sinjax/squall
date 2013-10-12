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
import org.openimaj.squall.compile.data.IOperation;
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
public class TestJenaRuleCompilerGreedyOrchestrator {
	
	private final class PrintAllOperation implements IOperation<Context> {
		@Override
		public void setup() {
			System.out.println("Starting Test");
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void perform(Context object) {
			System.out.println(object);
		}
	}

	private SourceRulePair nojoinRules;
	
	
	@org.junit.Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private File output;
	private File input;


	private SourceRulePair singlejoinRules;


	private SourceRulePair singlejoinComplexRules;


	private SourceRulePair allRules;


	private SourceRulePair singlefunctorRules;
	
	/**
	 * @throws IOException 
	 * 
	 */
	@Before
	public void before() throws IOException{
		InputStream nTripleStream = ReteTopologyTest.class.getResourceAsStream("/test.rdfs");
		
		Stream<Context> tripleContextStream = 
			new CollectionStream<Triple>(JenaUtils.readNTriples(nTripleStream))
			.map(new ContextWrapper("triple")
		);
		
		nojoinRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.nojoin.rules"));
		singlejoinRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.singlejoin.rules"));
		singlefunctorRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.singlefunctor.rules"));
		singlejoinComplexRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.singlejoin.complex.rules"));
		allRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.rules"));
		
	}



	private List<Rule> loadRules(String stream) {
		InputStream ruleStream = TestJenaRuleCompilerGreedyOrchestrator.class.getResourceAsStream(stream);
		List<Rule> rules = JenaUtils.readRules(ruleStream);
		return rules;
	}

	
	
	/**
	 * 
	 */
	@Test
	public void testBuilderNoJoin(){
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		ContextCPS comp = jrc.compile(nojoinRules);
		GreedyOrchestrator go = new GreedyOrchestrator();
		IOperation<Context> op = new PrintAllOperation();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		OIStreamBuilder builder = new OIStreamBuilder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderSingleJoin(){
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		ContextCPS comp = jrc.compile(singlejoinRules);
		GreedyOrchestrator go = new GreedyOrchestrator();
		IOperation<Context> op = new PrintAllOperation();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		OIStreamBuilder builder = new OIStreamBuilder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderSingleComplexJoin(){
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		ContextCPS comp = jrc.compile(singlejoinComplexRules);
		GreedyOrchestrator go = new GreedyOrchestrator();
		IOperation<Context> op = new PrintAllOperation();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		OIStreamBuilder builder = new OIStreamBuilder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderSingleFunctorJoin(){
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		ContextCPS comp = jrc.compile(singlefunctorRules);
		GreedyOrchestrator go = new GreedyOrchestrator();
		IOperation<Context> op = new PrintAllOperation();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		OIStreamBuilder builder = new OIStreamBuilder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderAll(){
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		ContextCPS comp = jrc.compile(allRules);
		GreedyOrchestrator go = new GreedyOrchestrator();
		IOperation<Context> op = new PrintAllOperation();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		OIStreamBuilder builder = new OIStreamBuilder();
		builder.build(orchestrated);
	}

}
