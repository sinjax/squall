package org.openimaj.squall.revised.orchestrate;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.revised.compile.JoinComponent;
import org.openimaj.squall.revised.compile.data.IVFunction;
import org.openimaj.squall.orchestrate.exception.CompleteCPSPlanningException;
import org.openimaj.squall.orchestrate.exception.IncompleteCPSPlanningException;
import org.openimaj.util.data.Context;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class CompleteCPSResult extends ArrayList<NamedNode<? extends IVFunction<Context, Context>>>
							implements CPSResult {

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public List<NamedNode<? extends IVFunction<Context, Context>>> getResults()
			throws IncompleteCPSPlanningException {
		return this;
	}

	@Override
	public void add(List<JoinComponent<?>> jcs,
			List<IVFunction<Context, Context>> preds) throws CompleteCPSPlanningException {
		throw new CompleteCPSPlanningException();
	}

	@Override
	public List<IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>>> getProcessingOptions()
			throws CompleteCPSPlanningException {
		throw new CompleteCPSPlanningException("Processing options are no longer accessible once they are finalised.");
	}

}
