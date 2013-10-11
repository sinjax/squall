package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.IOperation;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Given a {@link CompiledProductionSystem}, produce an {@link OrchestratedProductionSystem} ready 
 * to be built
 * @param <INPUT> The input of the orchestrator
 * @param <OUTPUT> The output of the orchestrator 
 *
 */
public interface Orchestrator<INPUT,OUTPUT> {
	/**
	 * @param sys the system to be orchestrated
	 * @param op the final operation (given the output of all final consequences)
	 * @return an {@link OrchestratedProductionSystem} ready to be built
	 */
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem<INPUT,OUTPUT> sys, IOperation<OUTPUT> op);
}
