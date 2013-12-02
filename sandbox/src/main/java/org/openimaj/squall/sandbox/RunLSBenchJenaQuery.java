package org.openimaj.squall.sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.rif.RIFRuleSet;
import org.openimaj.rif.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.rif.utils.RifUtils;
import org.openimaj.squall.build.Builder;
import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.source.URIProfileISourceFactory;
import org.openimaj.squall.compile.functions.rif.external.ExternalLoader;
import org.openimaj.squall.compile.jena.JenaRuleCompiler;
import org.openimaj.squall.compile.jena.SourceRulePair;
import org.openimaj.squall.compile.rif.RIFCoreRuleCompiler;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.CombinedISource;
import org.openimaj.squall.orchestrate.greedy.CombinedSourceGreedyOrchestrator;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.data.JoinStream;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.sun.media.jai.opimage.RIFUtil;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RunLSBenchJenaQuery {
	private static final class PrintAllOperation implements IOperation<Context>,Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1409401688854637882L;
		private static final Logger logger = Logger.getLogger(PrintAllOperation.class);
		@Override
		public void setup() {
			System.out.println("Starting Test");
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void perform(Context object) {
			logger.info("Final output:" + object);
		}
	}
	
	
	private static List<Rule> loadRules(String stream) {
		InputStream ruleStream = RunLSBenchJenaQuery.class.getResourceAsStream(stream);
		List<Rule> rules = JenaUtils.readRules(ruleStream);
		return rules;
	}
	/**
	 * @param args
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {
		URIProfileISourceFactory fact = URIProfileISourceFactory.instance();
		
		CombinedISource streams = new CombinedISource();
		streams.add(fact.createSource(new URI("file://lsbench/data/rdfPostStream1000.nt"), URIProfileISourceFactory.TURTLE_URI));
		streams.add(fact.createSource(new URI("file://lsbench/data/rdfPhotoStream1000.nt"), URIProfileISourceFactory.TURTLE_URI));
		streams.add(fact.createSource(new URI("file://lsbench/data/rdfPhotoLikeStream1000.nt"), URIProfileISourceFactory.TURTLE_URI));
		streams.add(fact.createSource(new URI("file://lsbench/data/gpsStream1000.nt"), URIProfileISourceFactory.TURTLE_URI));
		streams.add(fact.createSource(new URI("file://lsbench/data/mr0_sibdataset1000.nt"), URIProfileISourceFactory.TURTLE_URI));
		streams.add(fact.createSource(new URI("file://lsbench/data/rdfPostLikeStream1000.nt"), URIProfileISourceFactory.TURTLE_URI));
		
		IOperation<Context> op = new PrintAllOperation();

		JenaRuleCompiler jrc = new JenaRuleCompiler();
		// 83s for 3.5m triples ~= 42k triples/s
//		List<Rule> rule = loadRules("/lsbench/queries/simplejenaquery.rule"); 
		// 40s for 400k triples, 72s for 1m triples, 120s for 1.8m triples, 212s for 3.5m triples ~= 16k triples/s
//		List<Rule> rule = loadRules("/lsbench/queries/simplejoinjenaquery.rule");
		// 21s for 100k, 38s for 380k, 90s for 1.3m
		List<Rule> rule = loadRules("/lsbench/queries/2filterjenaquery.rule");  
		SourceRulePair lsbenchRules = SourceRulePair.simplePair(streams, rule );
		CompiledProductionSystem comp = jrc.compile(lsbenchRules );
		
		GreedyOrchestrator go = new CombinedSourceGreedyOrchestrator();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		
		Builder builder = StormStreamBuilder.localClusterBuilder(-1);
		builder.build(orchestrated);
	}
}
