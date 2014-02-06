package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.compile.JoinComponent;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
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
public class PartialCPSResult implements CPSResult {

	List<IndependentPair<List<JoinComponent<?>>, List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>>>> options;
	
	/**
	 * 
	 */
	public PartialCPSResult(){
		this.options = new ArrayList<IndependentPair<List<JoinComponent<?>>, List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>>>>();
	}
	
	@Override
	public void add(List<JoinComponent<?>> jcs, List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>> preds){
		this.options.add(
				new IndependentPair<List<JoinComponent<?>>, List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>>>(
						jcs, preds
				)
		);
	}
	
	@Override
	public boolean isComplete() {
		return false;
	}
	
	@Override
	public List<RuleWrapped<? extends NamedNode<? extends IFunction<Context, Context>>>> getResults() throws IncompleteCPSPlanningException{
		throw new IncompleteCPSPlanningException("This CPS does not provide any consequences, and so has not yet been provided with a plan.");
	}

	@Override
	public List<IndependentPair<List<JoinComponent<?>>, List<RuleWrappedPredicateFunction<? extends BasePredicateFunction>>>> getProcessingOptions()
			throws CompleteCPSPlanningException {
		return this.options;
	}

}
