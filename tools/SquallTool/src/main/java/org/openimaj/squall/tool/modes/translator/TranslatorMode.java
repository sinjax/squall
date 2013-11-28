package org.openimaj.squall.tool.modes.translator;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.tool.SquallToolOptions;
import org.openimaj.squall.tool.SquallToolSetup;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public abstract class TranslatorMode implements SquallToolSetup{
	/**
	 * @return return a CPS
	 */
	public abstract CompiledProductionSystem cps();
	
	@Override
	public void setup(SquallToolOptions opts) {
	}
	
	@Override
	public void shutdown(SquallToolOptions opts) {	
	}
}
