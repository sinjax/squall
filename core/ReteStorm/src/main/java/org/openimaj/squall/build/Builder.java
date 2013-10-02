package org.openimaj.squall.build;

import org.openimaj.squall.orchestrate.DirectedAcyclicGraph;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Given an {@link OrchestratedProductionSystem}, use it's {@link DirectedAcyclicGraph} to
 * build a production system
 *
 */
public interface Builder {
	/**
	 * @param ops
	 */
	public void build(OrchestratedProductionSystem ops);
}
