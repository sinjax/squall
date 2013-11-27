package org.openimaj.squall.tool.modes.translator;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.tool.SquallToolSetup;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public interface TranslatorMode extends SquallToolSetup{
	/**
	 * @return return a CPS
	 */
	public CompiledProductionSystem cps();
}
