package org.openimaj.squall.tool.modes.translator;

import java.net.URI;
import java.net.URISyntaxException;

import org.kohsuke.args4j.Option;
import org.openimaj.rifcore.RIFRuleSet;
import org.openimaj.rifcore.utils.RifUtils;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.rif.RIFCoreRuleCompiler;
import org.openimaj.squall.functions.rif.external.ExternalLoader;
import org.openimaj.squall.tool.SquallToolOptions;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFTranslatorMode extends TranslatorMode {

	private enum Profile {
		
		CORE("http://www.w3.org/ns/entailment/Core")
		;
		
		private URI profile;
		private Profile(String profileURI){
			try {
				this.profile = new URI(profileURI);
			} catch (URISyntaxException e) {
				throw new Error("Incorrectly defined profile enum value.", e);
			}
		}
		
		public URI getURI(){
			return this.profile;
		}
	}
	
	/**
	 * The rif document uri for the rules to load
	 */
	@Option(
			name = "--rif-rules",
			aliases = "-rifr",
			required = true,
			usage = "Load the rif rules from this URI",
			metaVar = "STRING")
	public String rifRuleURI = null;
	
	/**
	 * the rif profile according to which the rif document to load should be interpretted.
	 */
	@Option(
			name = "--rif-profile",
			aliases = "-rifp",
			required = false,
			usage = "Load the rif rules according to this profile specification")
	public Profile rifProfile = Profile.CORE;
	
	@Override
	public CompiledProductionSystem cps() {
		RIFCoreRuleCompiler rif = new RIFCoreRuleCompiler();
		return rif.compile(createRifRuleSet());
	}

	private RIFRuleSet createRifRuleSet() {
		RIFRuleSet readRules = RifUtils.readRules(rifRuleURI, rifProfile.getURI());
		return readRules;
	}
	
	@Override
	public void setup(SquallToolOptions opts) {
		ExternalLoader.loadExternals();	
	}

}
