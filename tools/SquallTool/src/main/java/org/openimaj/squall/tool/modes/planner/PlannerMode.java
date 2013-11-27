package org.openimaj.squall.tool.modes.planner;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.tool.SquallToolOptions;
import org.openimaj.squall.tool.SquallToolSetup;
import org.openimaj.util.data.Context;

/**
 * Hands a {@link CompiledProductionSystem} to a Planner returning 
 * a {@link OrchestratedProductionSystem}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class PlannerMode implements SquallToolSetup{
	private IOperation<Context> op;

	/**
	 * @param cps
	 * @return an ops given a cps
	 */
	public abstract OrchestratedProductionSystem ops(CompiledProductionSystem cps);
	
	/**
	 * @param op the operation
	 */
	public void setOperation(IOperation<Context> op){
		this.op = op;
	}

	IOperation<Context> getOperation() {
		return op;
	}
	
	public void setup(SquallToolOptions opts) { }
}