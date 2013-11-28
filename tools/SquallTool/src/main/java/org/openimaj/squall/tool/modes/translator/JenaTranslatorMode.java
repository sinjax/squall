package org.openimaj.squall.tool.modes.translator;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.jena.JenaRuleCompiler;
import org.openimaj.squall.compile.jena.SourceRulePair;
import org.openimaj.squall.tool.SquallToolOptions;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JenaTranslatorMode extends TranslatorMode {

	@Override
	public CompiledProductionSystem cps() {
		JenaRuleCompiler comp = new JenaRuleCompiler();
		
		return comp.compile(createSourceRule());
	}

	private SourceRulePair createSourceRule() {
		return null;
	}

	

}
