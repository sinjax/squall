package org.openimaj.squall.tool.modes.translator.builder;

import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.tool.SquallToolOptions;
import org.openimaj.squall.tool.SquallToolSetup;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class BuilderMode implements SquallToolSetup{
	/**
	 * @param ops build and run the OPS
	 */
	public abstract void run(OrchestratedProductionSystem ops);
	
	@Override
	public void setup(SquallToolOptions opts) {	
	}
	
	@Override
	public void shutdown(SquallToolOptions opts) {	
	}
}
