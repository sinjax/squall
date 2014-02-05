package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.compile.JoinComponent;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.data.RuleWrapped;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;
import org.openimaj.squall.orchestrate.exception.CompleteCPSPlanningException;
import org.openimaj.squall.orchestrate.exception.IncompleteCPSPlanningException;
import org.openimaj.util.data.Context;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class CompleteCPSResult extends ArrayList<NamedNode<? extends RuleWrapped<? extends IFunction<Context, Context>>>>
							implements CPSResult {

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public List<NamedNode<? extends RuleWrapped<? extends IFunction<Context, Context>>>> getResults()
			throws IncompleteCPSPlanningException {
		return this;
	}

	@Override
	public void add(List<JoinComponent<?>> jcs,
			List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>> preds) throws CompleteCPSPlanningException {
		throw new CompleteCPSPlanningException();
	}

	@Override
	public List<IndependentPair<List<JoinComponent<?>>, List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>>>> getProcessingOptions()
			throws CompleteCPSPlanningException {
		throw new CompleteCPSPlanningException("Processing options are no longer accessible once they are finalised.");
	}

}
