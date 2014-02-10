package org.openimaj.squall.orchestrate;

import java.util.List;

import org.openimaj.squall.compile.JoinComponent;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.data.RuleWrapped;
import org.openimaj.squall.functions.predicates.BasePredicateFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;
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
	public List<RuleWrapped<? extends NamedNode<? extends IFunction<Context, Context>>>> getResults() throws IncompleteCPSPlanningException;
	
	/**
	 * @param jcs
	 * @param preds
	 * @throws CompleteCPSPlanningException 
	 */
	public void add(List<JoinComponent<?>> jcs, List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>> preds) throws CompleteCPSPlanningException;
	
	/**
	 * @return
	 * @throws CompleteCPSPlanningException
	 */
	public List<IndependentPair<List<JoinComponent<?>>,List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>>>> getProcessingOptions() throws CompleteCPSPlanningException;
	
}
