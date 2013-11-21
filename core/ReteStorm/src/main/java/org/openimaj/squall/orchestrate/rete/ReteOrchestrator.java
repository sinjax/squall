package org.openimaj.squall.orchestrate.rete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.JoinComponent;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.orchestrate.NNIVFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReteOrchestrator implements Orchestrator{

	private HashMap<String, NamedNode<IVFunction<Context,Context>>> funcMap;
	private int consequence = 0;
	private int predicate = 0;
	private int join = 0;
	private int filter = 0;

	@Override
	public  OrchestratedProductionSystem orchestrate(CompiledProductionSystem sys, IOperation<Context> op) {
		
		OrchestratedProductionSystem ret = new OrchestratedProductionSystem();		
		ret.root = new ArrayList<NamedSourceNode>();
		orchestrateSources(sys,ret);
		NamedNode<? extends IVFunction<Context, Context>> finalsys = orchestrate(ret,sys);
		if(finalsys != null){			
			orchestrateOperation(ret,op, finalsys);
		} else {
			orchestrateOperation(ret,op);
		}
		return ret;
	}

	private void orchestrateOperation(OrchestratedProductionSystem ret, IOperation<Context> op) {
		NamedNode<?> opNode = new NGNOperation(ret, "OPERATION", op);
		for (NamedNode<?> namedNode : ret.getLeaves()) {			
			namedNode.connect(new NamedStream("link"), opNode);
		}
	}

	private void orchestrateOperation(OrchestratedProductionSystem ret, IOperation<Context> op, NamedNode<? extends IVFunction<Context, Context>> finalsys) {
		NamedNode<?> opNode = new NGNOperation(ret, "OPERATION", op);
		finalsys.connect(new NamedStream("link"), opNode);
	}

	private void orchestrateSources(CompiledProductionSystem sys,OrchestratedProductionSystem root) {
		if(sys.getSources().size()>0){
			for (ISource<Stream<Context>> sourceS: sys.getSources()) {				
				root.root.add(new NamedSourceNode(root,nextSourceName(root), sourceS));
			}
		}
		
	}

	private NamedNode<? extends IVFunction<Context,Context>> orchestrate(OrchestratedProductionSystem root,CompiledProductionSystem sys) {
		return null;
		
	}


	@SuppressWarnings("unused")
	/**
	 * FIXME: Make aggregation do something
	 * 
	 * @param currentNode
	 * @param aggregations
	 */
	private void orchestrateAggregations(NamedNode<?> currentNode, List<Function<Context, Context>> aggregations) {
		
	}

	private String nextSourceName(OrchestratedProductionSystem ret) {
		return "source_" + ret.root.size();
	}

	private NamedNode<? extends IVFunction<Context,Context>> orchestrateConsequences(
			OrchestratedProductionSystem root,
			List<NamedNode<? extends IVFunction<Context, Context>>> joinedCPS,
			IVFunction<Context,Context> function) {
		NNIVFunction consequenceNode = new NNIVFunction(
			root,
			nextConsequenceName(), 
			function
		);
		for (NamedNode<?> namedNode : joinedCPS) {
			namedNode.connect(new NamedStream("link"), consequenceNode);
		}
		return consequenceNode;
	}

	private String nextConsequenceName() {
		return String.format("CONSEQUENCE_%d",consequence ++ );
	}

	private NamedNode<?  extends IVFunction<Context, Context>> orchestratePredicates(
			OrchestratedProductionSystem root,
			NamedNode<?  extends IVFunction<Context, Context>> currentNode,
			List<IVFunction<Context,Context>> list) {
		
		for (IVFunction<Context, Context> pred : list) {
			NNIVFunction prednode = new NNIVFunction(
				root,
				nextPredicateName(),
				pred
			);
			currentNode.connect(new NamedStream("link"), prednode);
			currentNode = prednode;
		}
		return currentNode;
	}

	private String nextPredicateName() {
		return String.format("PREDICATE_%d",predicate  ++ );
	}

	private NamedNode<? extends IVFunction<Context, Context>> orchestrateJoinComponents(
			OrchestratedProductionSystem root, 
			List<JoinComponent<?>> list
	) {
		
		NamedNode<? extends IVFunction<Context, Context>> ret = null;
		for (JoinComponent<?> jc : list) {
			NamedNode<? extends IVFunction<Context,Context>> next;
			if(jc.isFunction()){				
				IVFunction<Context, Context> typedComponent = jc.getTypedComponent();
				next = createFilterNode(root, typedComponent);
			} else if (jc.isCPS()){
				CompiledProductionSystem cps = jc.getTypedComponent();
				next = orchestrate(root, cps);
			} else{
				// ignore unknown join comp
				throw new RuntimeException();
			}
			if(ret == null){
				ret = next;
			}
			else{
				ret = createJoinNode(root, ret, next);
			}
		}
		return ret ;
	}
	
	
	private NamedNode<IVFunction<Context, Context>> createJoinNode(
			OrchestratedProductionSystem root,
			NamedNode<? extends IVFunction<Context, Context>> left,
			NamedNode<? extends IVFunction<Context, Context>> right) {
		// Create a new NGNIVFunction that performs the desired IVFunction.
		NGNJoin joined = new NGNJoin(root,nextJoinName(), left, right);
		// If the function already exists in the set of created functions, return the existing NGNIVFunction.
		if (funcMap.containsKey(joined.getFunction().anonimised())) {
			return funcMap.get(joined.getFunction().anonimised());
		}
		funcMap.put(joined.getFunction().anonimised(),joined);
		return joined;
	}

	private String nextJoinName() {
		return String.format("JOIN_%d",join  ++ );
	}

	private NamedNode<IVFunction<Context, Context>> createFilterNode(
			OrchestratedProductionSystem root,
			IVFunction<Context,Context> filterFunc) {
		// If the function already exists in the set of created functions, return the existing NGNIVFunction.
		if (funcMap.containsKey(filterFunc.anonimised())) {
			return funcMap.get(filterFunc.anonimised());
		}
		// Otherwise, create a new NGNIVFunction that performs the desired IVFunction.
		NNIVFunction currentNode = new NNIVFunction(
				root, 
				nextFilterName(), 
				filterFunc
		);
		for (NamedSourceNode input : root.root) {
			input.connect(new NamedStream("link"), currentNode);;
		}
		return currentNode;
	}

	private String nextFilterName() {
		return String.format("FILTER_%d",filter  ++);
	}

}
