package org.openimaj.squall.tool.modes.translator.builder;

import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.tool.SquallToolSetup;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface BuilderMode extends SquallToolSetup{
	/**
	 * @param ops build and run the OPS
	 */
	public void run(OrchestratedProductionSystem ops);
}
