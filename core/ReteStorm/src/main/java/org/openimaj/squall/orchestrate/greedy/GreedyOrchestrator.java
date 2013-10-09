package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * 
 * A greedy orchestrator joins filters in order, ignores duplicated filters 
 * Sub {@link CompiledProductionSystem} instances are also treated first, in order and greedily
 * 
 * Predicates are handled at the end
 * Aggregations are handled at the end
 * Groups are completely ignored
 * 
 * Consequences are dealt with at the end
 *
 */
public class GreedyOrchestrator implements Orchestrator<Context,Context>{
	
	private int consequence = 0;
	private int predicate = 0;
	private int filter = 0;
	private int join = 0;

	@Override
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem<Context,Context> sys) {
		OrchestratedProductionSystem ret = new OrchestratedProductionSystem();		
		ret.root = new ArrayList<NamedSourceNode>();
		orchestrateSources(sys,ret);
		orchestrate(ret,sys);
		return ret;
	}

	private void orchestrateSources(
			CompiledProductionSystem<Context, Context> sys,
			OrchestratedProductionSystem root) {
		if(sys.getSources().size()>0){
			for (IStream<Context> sourceS: sys.getSources()) {				
				root.root.add(new NamedSourceNode(nextSourceName(root), sourceS));
			}
		}
		for (List<CompiledProductionSystem<Context, Context>> syslist: sys.getSystems()) {
			for (CompiledProductionSystem<Context, Context> cps : syslist) {
				orchestrateSources(cps, root);
			}
		}
	}

	private NamedNode<? extends IVFunction<Context,Context>> orchestrate(OrchestratedProductionSystem root,CompiledProductionSystem<Context,Context> sys) {
		NamedNode<? extends IVFunction<Context, Context>> combinedFilters = orchestrateFilters(root,sys.getFilters());
		combinedFilters = orchestratePredicates(combinedFilters,sys.getPredicates());
		
		List<NamedNode<? extends IVFunction<Context, Context>>> joinedCPS = new ArrayList<NamedNode<? extends IVFunction<Context, Context>>>();
		for (List<CompiledProductionSystem<Context,Context>> subsyslist : sys.getSystems()) {
			NamedNode<? extends IVFunction<Context, Context>> combined = null;
			for (CompiledProductionSystem<Context, Context> subsys : subsyslist) {
				NamedNode<? extends IVFunction<Context, Context>> next = orchestrate(root,subsys);
				if(combined == null){
					combined = next;
				} else{
					combined = createJoinNode(combined, next);
				}
			}
			if(combined!=null && combinedFilters != null){ // join the sub systems to any filters
				combined = createJoinNode(combined,combinedFilters);
			}
			joinedCPS.add(combined);
		}
		if(joinedCPS.size() == 0){
			// There were no sub CPS to join with, just add the combined filters to the list
			joinedCPS.add(combinedFilters);
		}
//		aggregations = orchestrateAggregations(joinedCPS,sys.getAggregations());
		return orchestrateConsequences(joinedCPS,sys.getConequences());
	}


	private void orchestrateAggregations(
			NamedNode<?> currentNode,
			List<Function<List<Map<String, String>>, Map<String, String>>> aggregations) {
		// TODO Auto-generated method stub
		
	}

	private String nextSourceName(OrchestratedProductionSystem ret) {
		return "source_" + ret.root.size();
	}

	private NamedNode<? extends IVFunction<Context,Context>> orchestrateConsequences(
			List<NamedNode<? extends IVFunction<Context, Context>>> joinedCPS,
			IVFunction<Context,Context> function) {
		NamedIVFunctionNode consequenceNode = new NamedIVFunctionNode(
			nextConsequenceName(), 
			function
		);
		for (NamedNode<?> namedNode : joinedCPS) {
			namedNode.connect(new NamedStream("link", namedNode, consequenceNode), consequenceNode);
		}
		return consequenceNode;
	}

	private String nextConsequenceName() {
		return String.format("CONSEQUENCE_%d",consequence++ );
	}

	private NamedNode<?  extends IVFunction<Context, Context>> orchestratePredicates(
			NamedNode<?  extends IVFunction<Context, Context>> currentNode,
			List<IVFunction<Context,Context>> list) {
		
		for (IVFunction<Context, Context> pred : list) {
			NamedIVFunctionNode prednode = new NamedIVFunctionNode(
				nextPredicateName(),
				pred
			);
			currentNode.connect(new NamedStream("link", currentNode, prednode), prednode);
			currentNode = prednode;
		}
		return currentNode;
	}

	private String nextPredicateName() {
		return String.format("PREDICATE_%d",predicate ++ );
	}

	private NamedNode<? extends IVFunction<Context, Context>> orchestrateFilters(
			OrchestratedProductionSystem root, 
			List<IVFunction<Context,Context>> list
	) {
		
		NamedNode<? extends IVFunction<Context, Context>> ret = null;
		for (IVFunction<Context,Context> filter : list) {
			NamedNode<? extends IVFunction<Context,Context>> next = createFilterNode(root, filter);
			if(ret == null){
				ret = next;
			}
			else{
				ret = createJoinNode(ret, next);
			}
		}
		return ret ;
	}

	private NamedNode<? extends IVFunction<Context, Context>> createJoinNode(
			NamedNode<? extends IVFunction<Context, Context>> left,
			NamedNode<? extends IVFunction<Context, Context>> right) {
		
		NamedJoinNode joined = new NamedJoinNode(nextJoinName(), left, right);
		return joined;
	}

	private String nextJoinName() {
		return String.format("JOIN_%d",join ++ );
	}

	private NamedIVFunctionNode createFilterNode(
			OrchestratedProductionSystem root,
			IVFunction<Context,Context> filterFunc) {
		NamedIVFunctionNode currentNode = new NamedIVFunctionNode(
				nextFilterName(), 
				filterFunc
		);
		for (NamedSourceNode input : root.root) {
			input.connect(new NamedStream("link", input, currentNode), currentNode);;
		}
		return currentNode;
	}

	private String nextFilterName() {
		return String.format("FILTER_%d",filter ++);
	}

}
