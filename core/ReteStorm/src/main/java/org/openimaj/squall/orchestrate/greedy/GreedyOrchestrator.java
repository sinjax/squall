package org.openimaj.squall.orchestrate.greedy;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.ComponentInformationFunction;
import org.openimaj.squall.compile.data.ComponentInformationPredicate;
import org.openimaj.squall.orchestrate.ComponentInformationFunctionNode;
import org.openimaj.squall.orchestrate.DAGNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Predicate;

import com.hp.hpl.jena.graph.Triple;

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
public class GreedyOrchestrator implements Orchestrator{

	@Override
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem sys) {
		OrchestratedProductionSystem ret = new OrchestratedProductionSystem();
		orchestrate(ret.graph,sys);
		return ret;
	}

	private void orchestrate(DAGNode<ComponentInformationFunctionNode> ret,CompiledProductionSystem sys) {
		for (CompiledProductionSystem subsys : sys.getSystems()) {
			orchestrate(ret,subsys);
		}
		ret = orchestrateFilters(ret,sys.getFilters());
		ret = orchestratePredicates(ret,sys.getPredicates());
		orchestrateConsequences(ret,sys.getConequences());
	}


	private void orchestrateConsequences(
			DAGNode<ComponentInformationFunctionNode> ret,
			List<Function<Map<String, String>, ?>> conequences) {
		for (Function<Map<String, String>, ?> function : conequences) {
			ComponentInformationFunctionNode consequenceNode = new ComponentInformationFunctionNode(
				null, 
				ContextFunction.wrap("binding","consequence",function)
			);
			ret.addChild(consequenceNode);
		}
	}

	private DAGNode<ComponentInformationFunctionNode> orchestratePredicates(
			DAGNode<ComponentInformationFunctionNode> ret,
			List<ComponentInformationPredicate<Map<String, String>>> predicates) {
		
		for (ComponentInformationPredicate<Map<String,String>> pred : predicates) {
			ComponentInformationFunctionNode prednode = new ComponentInformationFunctionNode(
					pred.information(), 
					ContextFunction.wrap("binding","binding",new FilterFunction<Map<String,String>>(pred))
				);
			ret.addChild(prednode);
			ret = prednode;
		}
		return ret;
	}

	private DAGNode<ComponentInformationFunctionNode> orchestrateFilters(
			DAGNode<ComponentInformationFunctionNode> ret,
			List<ComponentInformationFunction<Triple, Map<String, String>>> list
	) {
		Iterator<ComponentInformationFunction<Triple, Map<String, String>>> filterIter = list.iterator();
		if(ret == null)
			ret = createFilterNode(ret, filterIter.next());
		while(filterIter.hasNext()){
			DAGNode<ComponentInformationFunctionNode> nextNode = createFilterNode(ret, filterIter.next());
			ret = createJoinNode(ret,nextNode);
		}
		
		return currentNode;
	}

	private DAGNode<ComponentInformationFunctionNode> createJoinNode(
			ComponentInformationFunctionNode left,
			ComponentInformationFunctionNode right) {
		ComponentInformationFunctionNode currentNode = new ComponentInformationJoinNode(left.information(),right.information());
		left.addChild(currentNode);
		right.addChild(currentNode);
		return currentNode;
	}

	private ComponentInformationFunctionNode createFilterNode(
			DAGNode<ComponentInformationFunctionNode> ret,
			ComponentInformationFunction<Triple, Map<String, String>> currentFilter) {
		ComponentInformationFunctionNode currentNode = new ComponentInformationFunctionNode(
				currentFilter.information(), 
				ContextFunction.wrap("triple","binding",currentFilter)
		);
		ret.addChild(currentNode);
		return currentNode;
	}

}
