package org.openimaj.squall.tool.modes.planner;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.CombinedSourceGreedyOrchestrator;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CombinedSourceGreedyPlannerMode extends PlannerMode {

	@Override
	public OrchestratedProductionSystem ops(CompiledProductionSystem cps) {
		return new CombinedSourceGreedyOrchestrator().orchestrate(cps, getOperation());
	}


}
