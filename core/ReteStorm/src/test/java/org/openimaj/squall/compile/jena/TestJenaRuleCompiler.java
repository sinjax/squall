package org.openimaj.squall.compile.jena;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.FileUtils;
import org.openimaj.rdf.storm.topology.ReteTopologyTest;
import org.openimaj.squall.build.OIStreamBuilder;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.TripleTripleListCPS;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.IStreamWrapper;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestJenaRuleCompiler {
	
	private SourceRulePair sourceRules;
	private List<IStream<Context>> sources;
	private Collection<Triple> tripleCol;
	@org.junit.Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private File output;
	private File input;
	
	class ContextWrapper implements Function<Triple,Context>{

		@Override
		public Context apply(Triple in) {
			Context ret = new Context();
			ret.put("triple", in);
			return ret;
		}
		
	}
	/**
	 * @throws IOException 
	 * 
	 */
	@Before
	public void before() throws IOException{
		tripleCol = new ArrayList<Triple>();
		input = folder.newFile("triples");
		InputStream inputStream = ReteTopologyTest.class.getResourceAsStream("/test.rdfs");
		FileUtils.copyStreamToFile(inputStream, this.input);
		RiotReader.createParserNTriples(input.toURI().toURL().openStream(), new Sink<Triple>() {
			
			@Override
			public void close() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void send(Triple item) {
				tripleCol.add(item);
			}
			
			@Override
			public void flush() {
			}
		}).parse();
		
		sources = new ArrayList<IStream<Context>>();
		Initialisable init = new Initialisable() {
			
			@Override
			public void setup() { }
			
			@Override
			public void cleanup() { }
		};
		sources.add(
			new IStreamWrapper<Context>(
				new CollectionStream<Triple>(tripleCol)
				.map(new ContextWrapper())
			,init)
		);
		List<Rule> rules = JenaUtils.readRules(TestJenaRuleCompiler.class.getResourceAsStream("/test.single.rules"));
		sourceRules = new SourceRulePair(sources, rules);
		
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
