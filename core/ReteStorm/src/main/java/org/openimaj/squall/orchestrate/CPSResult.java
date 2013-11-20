package org.openimaj.squall.orchestrate;

import java.util.List;

import org.openimaj.squall.compile.JoinComponent;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.orchestrate.exception.CompleteCPSPlanningException;
import org.openimaj.squall.orchestrate.exception.IncompleteCPSPlanningException;
import org.openimaj.util.data.Context;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public interface CPSResult {

	/**
	 * @return
	 */
	public boolean isComplete();
	
	/**
	 * @return
	 * @throws IncompleteCPSPlanningException
	 */
	public List<NamedNode<? extends IVFunction<Context, Context>>> getResults() throws IncompleteCPSPlanningException;
	
	/**
	 * @param jcs
	 * @param preds
	 * @throws CompleteCPSPlanningException 
	 */
	public void add(List<JoinComponent<?>> jcs, List<IVFunction<Context, Context>> preds) throws CompleteCPSPlanningException;
	
	/**
	 * @return
	 * @throws CompleteCPSPlanningException
	 */
	public List<IndependentPair<List<JoinComponent<?>>,List<IVFunction<Context,Context>>>> getProcessingOptions() throws CompleteCPSPlanningException;
	
}
