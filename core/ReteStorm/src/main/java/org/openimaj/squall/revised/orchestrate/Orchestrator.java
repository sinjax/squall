package org.openimaj.squall.revised.orchestrate;

import org.openimaj.squall.revised.compile.CompiledProductionSystem;
import org.openimaj.squall.revised.compile.data.IOperation;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Given a {@link CompiledProductionSystem}, produce an {@link OrchestratedProductionSystem} ready 
 * to be built
 *
 */
public interface Orchestrator {
	/**
	 * @param sys the system to be orchestrated
	 * @param op the final operation (given the output of all final consequences)
	 * @return an {@link OrchestratedProductionSystem} ready to be built
	 */
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem sys, IOperation<Context> op);
}
