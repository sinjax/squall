package org.openimaj.demos;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.ReteTopologyTest;
import org.openimaj.rif.RIFRuleSet;
import org.openimaj.rif.conditions.atomic.RIFAtom;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.rif.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.squall.build.OIStreamBuilder;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.functions.rif.predicates.NumericRIFPredicateFunction;
import org.openimaj.squall.compile.jena.JenaRuleCompiler;
import org.openimaj.squall.compile.jena.SourceRulePair;
import org.openimaj.squall.compile.jena.TestJenaRuleCompilerGreedyOrchestratorOIBuilder;
import org.openimaj.squall.compile.rif.RIFCoreRuleCompiler;
import org.openimaj.squall.compile.rif.TestRifRuleCompilerGreedyOrchestratorStormBuilder;
import org.openimaj.squall.compile.rif.provider.ExternalFunctionProvider;
import org.openimaj.squall.compile.rif.provider.ExternalFunctionRegistry;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.squall.utils.OPSDisplayUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class VisRIFGreedyRule {
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
		InputStream ruleStream = VisRIFGreedyRule.class.getResourceAsStream(stream);
		List<Rule> rules = JenaUtils.readRules(ruleStream);
		return rules;
	}
	
	private static final class GeoInHaversineDistanceProvider extends ExternalFunctionProvider {

		private static final class GeoInHaversineDistanceFunction extends NumericRIFPredicateFunction {

			/**
			 * 
			 */
			private static final long serialVersionUID = -7445044998530563542L;
			private static final long earthRadius = 6371;//kilometres
			private Node[] nodes;
			
			public GeoInHaversineDistanceFunction(Node[] ns)
					throws RIFPredicateException {
				super(ns);
				this.nodes = ns;
			}
			
			private double haversin(double pheta){
				return (1d - Math.cos(pheta)) / 2d;
			}

			@Override
			public List<Context> apply(Context in) {
				List<Context> ret = new ArrayList<Context>();
				Map<String,Node> binds = in.getTyped("bindings");
				
				Double maxDist = extractBinding(binds, nodes[0]);//kilometres
				Double long1 = Math.PI * extractBinding(binds, nodes[1]) / 180d;
				Double lat1 = Math.PI * extractBinding(binds, nodes[2]) / 180d;
				Double long2 = Math.PI * extractBinding(binds, nodes[3]) / 180d;
				Double lat2 = Math.PI * extractBinding(binds, nodes[4]) / 180d;
				
				Double distance = 2 * earthRadius * Math.asin(
														Math.sqrt(
															haversin(lat2 - lat1) +
															Math.cos(lat1) * Math.cos(lat2) * haversin(long2 - long1)
														)
													);
				if (distance <= maxDist) ret.add(in);
				
				return ret;
			}
			
		}
		
		@Override
		public IVFunction<Context, Context> apply(RIFExternalExpr in) {
			RIFAtom atom = in.getExpr().getCommand();
			try {
				return new GeoInHaversineDistanceFunction(extractNodes(atom));
			} catch (RIFPredicateException e) {
				throw new UnsupportedOperationException(e);
			}
		}

		@Override
		public IVFunction<Context, Context> apply(RIFExternalValue in) {
			RIFAtom atom = in.getVal();
			try {
				return new GeoInHaversineDistanceFunction(extractNodes(atom));
			} catch (RIFPredicateException e) {
				throw new UnsupportedOperationException(e);
			}
		}
		
	}
	
	public static void main(String[] args) {
		ExternalFunctionRegistry.register("http://www.ins.cwi.nl/sib/rif-builtin-predicate/geo-in-haversine-distance", new GeoInHaversineDistanceProvider());
		
		String ruleSource = "/lsbench/queries.rif";
		
		RIFRuleSet rules = null;
		RIFEntailmentImportProfiles profs = new RIFEntailmentImportProfiles();
		try {
			InputStream resourceAsStream = TestRifRuleCompilerGreedyOrchestratorStormBuilder.class.getResourceAsStream(ruleSource);
//			System.out.println(FileUtils.readall(resourceAsStream));
			rules = profs.parse(
					resourceAsStream,
					new URI("http://www.w3.org/ns/entailment/Core")
				);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		RIFCoreRuleCompiler jrc = new RIFCoreRuleCompiler();
		CompiledProductionSystem comp = jrc.compile(rules);
		GreedyOrchestrator go = new GreedyOrchestrator();
		IOperation<Context> op = new PrintAllOperation();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		
		OPSDisplayUtils.display(orchestrated);
	}
}
