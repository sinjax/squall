package org.openimaj.squall.orchestrate.rete;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.data.RuleWrapped;
import org.openimaj.squall.orchestrate.NNIFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.greedy.GreedyOrchestrator;
import org.openimaj.squall.orchestrate.greedy.StreamAwareFixedJoinFunction;
import org.openimaj.squall.orchestrate.greedy.StreamAwareFixedJoinFunction.RuleWrappedStreamAwareFixedJoinFunction;
import org.openimaj.util.data.Context;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TPLOReteOrchestrator extends GreedyOrchestrator{

	private HashMap<String, NNIFunction> funcMap = new HashMap<String, NNIFunction>();;
	
	/**
	 * @param capacity the capacity of the window
	 * @param duration the duration of time
	 * @param time the time unit
	 */
	public TPLOReteOrchestrator(int capacity, long duration, TimeUnit time) {
		super(capacity, duration, time);
	}
	
	/**
	 * 
	 */
	public TPLOReteOrchestrator(){
		super();
	}
	
	@Override
	protected RuleWrapped<? extends NamedNode<? extends IFunction<Context, Context>>> createJoinNode(
			OrchestratedProductionSystem root,
			RuleWrapped<? extends NamedNode<? extends IFunction<Context, Context>>> left,
			RuleWrapped<? extends NamedNode<? extends IFunction<Context, Context>>> right) {
		// Create a new NGNIVFunction that performs the desired IVFunction.
		RuleWrappedStreamAwareFixedJoinFunction join = StreamAwareFixedJoinFunction.ruleWrapped(
														left,
														right
											);
		RuleWrapped<NNIFunction> joined;
		
		// If the function already exists in the set of created functions, return the existing NGNIVFunction.
		if (funcMap.containsKey(join.getVariableHolder().identifier())) {
			joined = new RuleWrapped<NNIFunction>(
							join.getVariableHolder(),
							funcMap.get(join.getVariableHolder().identifier())
					);
		} else {
			joined = new RuleWrapped<NNIFunction>(
							join.getVariableHolder(),
							new NNIFunction(
									root,
									nextJoinName(),
									join.getWrapped()
							)
			);
			funcMap.put(joined.getVariableHolder().identifier(),joined.getWrapped());
		}
		
		List<String> lsv = join.getWrapped().leftSharedVars();
		String[] leftSharedVars = lsv.toArray(new String[lsv.size()]);
		
		List<String> rsv = join.getWrapped().rightSharedVars();
		String[] rightSharedVars = rsv.toArray(new String[rsv.size()]);
		
		NamedStream leftStream = new NamedStream(left.getVariableHolder().identifier(), leftSharedVars);
		left.getWrapped().connectOutgoingEdge(leftStream);
		join.getWrapped().setLeftStream(leftStream.identifier(), this.getWindowInformation());
		joined.getWrapped().connectIncomingEdge(leftStream);
		
		NamedStream rightStream = new NamedStream(right.getVariableHolder().identifier(), rightSharedVars);
		right.getWrapped().connectOutgoingEdge(rightStream);
		join.getWrapped().setRightStream(rightStream.identifier(), this.getWindowInformation());
		joined.getWrapped().connectIncomingEdge(rightStream);
		
		return joined;
	}

	@Override
	protected RuleWrapped<? extends NamedNode<? extends IFunction<Context,Context>>> createFilterNode(
			OrchestratedProductionSystem root,
			RuleWrapped<? extends IFunction<Context,Context>> filterFunc) {
		// If the function already exists in the set of created functions, return the existing NGNIVFunction.
		if (funcMap.containsKey(filterFunc.identifier())) {
			return new RuleWrapped<NNIFunction>(filterFunc.getVariableHolder(),funcMap.get(filterFunc.identifier()));
		}
		// Otherwise, create a new NGNIVFunction that performs the desired IVFunction.
		RuleWrapped<NNIFunction> currentNode = new RuleWrapped<NNIFunction>(
														filterFunc.getVariableHolder(),
														new NNIFunction(
																root, 
																nextFilterName(), 
																filterFunc.getWrapped()
														)
												);
		funcMap.put(currentNode.identifier(), currentNode.getWrapped());
		for (NamedSourceNode input : root.root) {
			NamedStream str = new NamedStream(input.getName());
			input.connectOutgoingEdge(str);
			currentNode.getWrapped().connectIncomingEdge(str);
		}
		return currentNode;
	}

}
