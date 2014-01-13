package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
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
public class PartialCPSResult implements CPSResult {

	List<IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>>> options;
	
	/**
	 * 
	 */
	public PartialCPSResult(){
		this.options = new ArrayList<IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>>>();
	}
	
	public void add(List<JoinComponent<?>> jcs, List<IVFunction<Context, Context>> preds){
		this.options.add(
				new IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>>(
						jcs, preds
				)
		);
	}
	
	@Override
	public boolean isComplete() {
		return false;
	}
	
	@Override
	public List<NamedNode<? extends IVFunction<Context, Context>>> getResults() throws IncompleteCPSPlanningException{
		throw new IncompleteCPSPlanningException("This CPS does not provide any consequences, and so has not yet been provided with a plan.");
	}

	@Override
	public List<IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>>> getProcessingOptions()
			throws CompleteCPSPlanningException {
		return this.options;
	}

}
