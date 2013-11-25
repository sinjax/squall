package org.openimaj.squall.compile.rif;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.rif.RIFRuleSet;
import org.openimaj.rif.conditions.atomic.RIFAtom;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.rif.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.squall.build.Builder;
import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.functions.rif.predicates.NumericRIFPredicateFunction;
import org.openimaj.squall.compile.rif.provider.ExternalFunctionProvider;
import org.openimaj.squall.compile.rif.provider.ExternalFunctionRegistry;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.util.data.Context;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.graph.Node;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestRifRuleCompilerGreedyOrchestratorStormBuilder {
	
	private static final class PrintAllOperation implements IOperation<Context>, Serializable {
		@Override
		public void setup() {
			System.out.println("Starting Test");
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void perform(Context object) {
			if(object.containsKey("inphoto"))System.out.println(object);
		}
	}



	private RIFRuleSet simpleRules;
	private RIFRuleSet simplejoinRules;
	private RIFRuleSet complexjoinRules;
	private RIFRuleSet multiunionRules;
	private RIFRuleSet lsbench;

	private RIFRuleSet readRules(String ruleSource) {
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
		return rules;
	}
	/**
	 * @throws IOException 
	 * 
	 */
	@Before
	public void before() throws IOException{
		this.simpleRules = readRules("/test.simple.rule.rif");
		this.simplejoinRules = readRules("/test.simplejoin.rule.rif");
		this.complexjoinRules = readRules("/test.complexjoin.rule.rif");
		this.multiunionRules = readRules("/test.multiunion.rule.rif");
		this.lsbench = readRules("/lsbench/queries.rif");
	}
	
	/**
	 * 
	 */
	@Test
	public void testSimpleRulesBuilder(){
		testRuleSet(simpleRules);
	}
	
	/**
	 * 
	 */
	@Test
	public void testSimpleJoinBuilder(){
		testRuleSet(simplejoinRules);
	}
	
	/**
	 * 
	 */
	@Test
	public void testComplexRules(){
		testRuleSet(complexjoinRules);
	}
	
	/**
	 * 
	 */
	@Test
	public void testMultiUnionRules(){
		testRuleSet(multiunionRules);
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
	
	/**
	 * 
	 */
	@Test
	public void testLSBenchRulesBuilder(){
		ExternalFunctionRegistry.register("http://www.ins.cwi.nl/sib/rif-builtin-predicate/geo-in-haversine-distance", new GeoInHaversineDistanceProvider());
		testRuleSet(lsbench);
	}
	
	private void testRuleSet(RIFRuleSet ruleSet) {
		IOperation<Context> op = new PrintAllOperation();

		RIFCoreRuleCompiler jrc = new RIFCoreRuleCompiler();
		CompiledProductionSystem comp = jrc.compile(ruleSet);
		
		GreedyOrchestrator go = new GreedyOrchestrator();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		
		Builder builder = StormStreamBuilder.localClusterBuilder(20000);
		builder.build(orchestrated);
	}
	

}
