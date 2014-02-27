package org.openimaj.squall.compile.jena;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.rdf.storm.topology.ReteTopologyTest;
import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.CountingOperation;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestJenaRuleCompilerGreedyOrchestratorStormBuilder {
	
	private static final class TestISource implements ISource<Stream<Context>> {
		private InputStream nTripleStream;

		@Override
		public Stream<Context> apply(Stream<Context> in) {
			return apply();
		}

		@Override
		public Stream<Context> apply() {
			return new CollectionStream<Triple>(JenaUtils.readNTriples(nTripleStream))
			.map(new ContextWrapper("triple"));
		}

		@Override
		public void setup() { 
			nTripleStream = ReteTopologyTest.class.getResourceAsStream("/test.rdfs");
		}

		@Override
		public void cleanup() {
			try {
				this.nTripleStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.nTripleStream = null;
		}

		@Override
		public void write(Kryo kryo, Output output) {
			System.out.println("writing jena test isource");
		}

		@Override
		public void read(Kryo kryo, Input input) {
			System.out.println("reading");
		}

		@Override
		public boolean isStateless() {
			return false;
		}

		@Override
		public boolean forcedUnique() {
			return true;
		}
	}

	private SourceRulePair nojoinRules;
	
	
	@org.junit.Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private SourceRulePair singlejoinRules;


	private SourceRulePair singlejoinComplexRules;


	private SourceRulePair allRules;


	private SourceRulePair singlefunctorRules;
	
	private SourceRulePair reentrantRules;
	
	/**
	 * @throws IOException 
	 * 
	 */
	@Before
	public void before() throws IOException{
		
		ISource<Stream<Context>> tripleContextStream = new TestISource();
		
		nojoinRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.nojoin.rules"));
		singlejoinRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.singlejoin.rules"));
		singlefunctorRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.singlefunctor.rules"));
		singlejoinComplexRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.singlejoin.complex.rules"));
		allRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.rules"));
		reentrantRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.reentrant.rules"));
		
	}


	private StormStreamBuilder builder() {
		StormStreamBuilder builder = StormStreamBuilder.localClusterBuilder(5000);
		return builder;
	}
	


	private JenaRuleCompiler compiler() {
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		return jrc;
	}



	private GreedyOrchestrator orchestrator() {
		GreedyOrchestrator go = new GreedyOrchestrator();
		return go;
	}
	
	
	private List<Rule> loadRules(String stream) {
		InputStream ruleStream = TestJenaRuleCompilerGreedyOrchestratorStormBuilder.class.getResourceAsStream(stream);
		List<Rule> rules = JenaUtils.readRules(ruleStream);
		return rules;
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderNoJoin(){
		JenaRuleCompiler jrc = compiler();
		ContextCPS comp = jrc.compile(nojoinRules);
		GreedyOrchestrator go = orchestrator();
		IOperation<Context> op = new CountingOperation(2);
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		StormStreamBuilder builder = builder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderSingleJoin(){
		JenaRuleCompiler jrc = compiler();
		ContextCPS comp = jrc.compile(singlejoinRules);
		GreedyOrchestrator go = orchestrator();
		IOperation<Context> op = new CountingOperation(1);
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		StormStreamBuilder builder = builder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderSingleComplexJoin(){
		JenaRuleCompiler jrc = compiler();
		ContextCPS comp = jrc.compile(singlejoinComplexRules);
		GreedyOrchestrator go = orchestrator();
		IOperation<Context> op = new CountingOperation(1);
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		StormStreamBuilder builder = builder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderSingleFunctorJoin(){
		JenaRuleCompiler jrc = compiler();
		ContextCPS comp = jrc.compile(singlefunctorRules);
		GreedyOrchestrator go = orchestrator();
		IOperation<Context> op = new CountingOperation(1);
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		StormStreamBuilder builder = builder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderReentrantRule(){
		JenaRuleCompiler jrc = compiler();
		ContextCPS comp = jrc.compile(reentrantRules);
		GreedyOrchestrator go = orchestrator();
		IOperation<Context> op = new CountingOperation(4);
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		StormStreamBuilder builder = builder();
		builder.build(orchestrated);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBuilderAll(){
		JenaRuleCompiler jrc = compiler();
		ContextCPS comp = jrc.compile(allRules);
		GreedyOrchestrator go = orchestrator();
		IOperation<Context> op = new CountingOperation(4);
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		StormStreamBuilder builder = builder();
		builder.build(orchestrated);
	}





}
