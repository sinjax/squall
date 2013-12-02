package org.openimaj.squall.tool.modes.translator;

import org.kohsuke.args4j.Option;
import org.openimaj.rif.RIFRuleSet;
import org.openimaj.rif.utils.RifUtils;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.functions.rif.external.ExternalLoader;
import org.openimaj.squall.compile.rif.RIFCoreRuleCompiler;
import org.openimaj.squall.tool.SquallToolOptions;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RIFTranslatorMode extends TranslatorMode {

	/**
	 * the ketrel queues for input and output
	 */
	@Option(
			name = "--rif-rules",
			aliases = "-rifr",
			required = true,
			usage = "Load the rif rules from this URI",
			metaVar = "STRING")
	public String rifRuleURI = null;
	
	@Override
	public CompiledProductionSystem cps() {
		RIFCoreRuleCompiler rif = new RIFCoreRuleCompiler();
		return rif.compile(createRifRuleSet());
	}

	private RIFRuleSet createRifRuleSet() {
		RIFRuleSet readRules = RifUtils.readRules(rifRuleURI);
		return readRules;
	}
	
	@Override
	public void setup(SquallToolOptions opts) {
		ExternalLoader.loadExternals();	
	}

}
