package org.openimaj.squall.sandbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.openimaj.rif.RIFRuleSet;
import org.openimaj.rif.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.squall.build.Builder;
import org.openimaj.squall.build.storm.StormStreamBuilder;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.functions.rif.external.ExternalLoader;
import org.openimaj.squall.compile.rif.RIFCoreRuleCompiler;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.util.data.Context;
import org.xml.sax.SAXException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RunLSBenchRIFQuery {
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
			if(object.get("rule").equals("http://www.ins.cwi.nl/sib/vocabulary/lsbench-query-7.5"))
			{
				logger.info("Final output from lsbench-query-7.5:" + object);
			}
		}
	}
	
	private static RIFRuleSet readRules(String ruleSource) {
		RIFRuleSet rules = null;
		RIFEntailmentImportProfiles profs = new RIFEntailmentImportProfiles();
		try {
			InputStream resourceAsStream = RunLSBenchRIFQuery.class.getResourceAsStream(ruleSource);
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
	 * @param args
	 */
	public static void main(String[] args) {
		ExternalLoader.loadExternals();
		RIFRuleSet lsbenchRules = readRules("/queries.rif");
		IOperation<Context> op = new PrintAllOperation();

		RIFCoreRuleCompiler jrc = new RIFCoreRuleCompiler();
		CompiledProductionSystem comp = jrc.compile(lsbenchRules);
		
		GreedyOrchestrator go = new GreedyOrchestrator();
		OrchestratedProductionSystem orchestrated = go.orchestrate(comp, op );
		
		Builder builder = StormStreamBuilder.localClusterBuilder(-1);
		builder.build(orchestrated);
	}
}
