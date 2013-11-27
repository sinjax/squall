package org.openimaj.squall.tool.modes.planner;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.squall.tool.SquallToolOptions;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GreedyPlannerMode extends PlannerMode {

	@Override
	public OrchestratedProductionSystem ops(CompiledProductionSystem cps) {
		GreedyOrchestrator gom = new GreedyOrchestrator();
		return gom.orchestrate(cps, getOperation());
	}

	

}
