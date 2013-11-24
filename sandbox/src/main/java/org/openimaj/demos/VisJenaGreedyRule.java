package org.openimaj.demos;

import java.io.InputStream;
import java.util.List;

import org.openimaj.rdf.storm.topology.ReteTopologyTest;
import org.openimaj.squall.build.OIStreamBuilder;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.jena.JenaRuleCompiler;
import org.openimaj.squall.compile.jena.SourceRulePair;
import org.openimaj.squall.compile.jena.TestJenaRuleCompilerGreedyOrchestratorOIBuilder;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.squall.utils.OPSDisplayUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class VisJenaGreedyRule {
	static class PrintAllOperation implements IOperation<Context> {
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
	static ISource<Stream<Context>> tripleContextStream = new ISource<Stream<Context>>() {
		
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
		public void cleanup() { }
	};
	
	private static List<Rule> loadRules(String stream) {
		InputStream ruleStream = VisJenaGreedyRule.class.getResourceAsStream(stream);
		List<Rule> rules = JenaUtils.readRules(ruleStream);
		return rules;
	}
	
	public static void main(String[] args) {
		SourceRulePair allRules = SourceRulePair.simplePair(tripleContextStream,loadRules("/test.two.rules"));
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		ContextCPS comp = jrc.compile(allRules);
		GreedyOrchestrator go = new GreedyOrchestrator();
		IOperation<Context> op = new PrintAllOperation();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		
		OPSDisplayUtils.display(orchestrated);
	}
}
