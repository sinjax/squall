package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.CompiledProductionSystem;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Given a {@link CompiledProductionSystem}, produce an {@link OrchestratedProductionSystem} ready 
 * to be built
 *
 */
public interface Orchestrator {
	/**
	 * @param sys
	 * @return an {@link OrchestratedProductionSystem} ready to be built
	 */
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem sys);
}
