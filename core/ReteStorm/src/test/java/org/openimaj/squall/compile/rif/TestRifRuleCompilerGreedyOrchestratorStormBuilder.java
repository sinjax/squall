package org.openimaj.squall.compile.rif;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.rifcore.RIFRuleSet;
import org.openimaj.rifcore.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.squall.build.Builder;
import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.CountingOperation;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.rif.providers.predicates.ExternalLoader;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.util.data.Context;
import org.xml.sax.SAXException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestRifRuleCompilerGreedyOrchestratorStormBuilder {


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
		testRuleSet(simpleRules,4);
	}
	
	/**
	 * 
	 */
	@Test
	public void testSimpleJoinBuilder(){
		testRuleSet(simplejoinRules,2);
	}
	
	/**
	 * 
	 */
	@Test
	public void testComplexRules(){
		testRuleSet(complexjoinRules,2);
	}
	
	/**
	 * 
	 */
	@Test
	public void testMultiUnionRules(){
		testRuleSet(multiunionRules,6);
	}
	
	/**
	 * 
	 */
	@Test
	public void testLSBenchRulesBuilder(){
		testRuleSet(lsbench,8);
	}
	
	private void testRuleSet(RIFRuleSet ruleSet, int expected) {
		ExternalLoader.loadExternals();
		
		IOperation<Context> op = new CountingOperation(expected);

		RIFCoreRuleCompiler jrc = new RIFCoreRuleCompiler();
		CompiledProductionSystem comp = jrc.compile(ruleSet);
		
		GreedyOrchestrator go = new GreedyOrchestrator();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		
		Builder builder = StormStreamBuilder.localClusterBuilder(5000);
		builder.build(orchestrated);
	}
	

}
