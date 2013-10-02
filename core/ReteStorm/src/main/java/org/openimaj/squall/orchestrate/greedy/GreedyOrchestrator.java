package org.openimaj.squall.orchestrate.greedy;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.ComponentInformationFunction;
import org.openimaj.squall.compile.data.ComponentInformationPredicate;
import org.openimaj.squall.orchestrate.ComponentInformationFunctionNode;
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
		ret.root = new ComponentInformationFunctionNode(sys.information(), null);
		orchestrate(ret.root,sys);
		return ret;
	}

	private ComponentInformationFunctionNode orchestrate(ComponentInformationFunctionNode root,CompiledProductionSystem sys) {
		ComponentInformationFunctionNode currentNode = null;
		// FIXME: The Sub systems must be joined!
		for (CompiledProductionSystem subsys : sys.getSystems()) {
			currentNode = orchestrate(root,subsys);
		}
		currentNode = orchestrateFilters(currentNode,root,sys.getFilters());
		currentNode = orchestratePredicates(currentNode,sys.getPredicates());
		orchestrateConsequences(currentNode,sys.getConequences());
		return currentNode;
	}


	private void orchestrateConsequences(
			ComponentInformationFunctionNode ret,
			List<Function<Map<String, String>, ?>> conequences) {
		for (Function<Map<String, String>, ?> function : conequences) {
			ComponentInformationFunctionNode consequenceNode = new ComponentInformationFunctionNode(
				null, 
				ContextFunction.wrap("binding","consequence",function)
			);
			ret.addChild(consequenceNode);
		}
	}

	private ComponentInformationFunctionNode orchestratePredicates(
			ComponentInformationFunctionNode ret,
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

	private ComponentInformationFunctionNode orchestrateFilters(
			ComponentInformationFunctionNode ret,
			ComponentInformationFunctionNode root, 
			List<ComponentInformationFunction<Triple, Map<String, String>>> list
	) {
		
		// Always add filters to the root directly, but join them to each other
		
		Iterator<ComponentInformationFunction<Triple, Map<String, String>>> filterIter = list.iterator();
		if(ret == null) // Nothing else to join with yet, add the first filter 
		{
			ret = createFilterNode(root, filterIter.next());
		}
		while(filterIter.hasNext()){
			ComponentInformationFunctionNode nextNode = createFilterNode(root, filterIter.next()); 
			ret = createJoinNode(ret,nextNode);
		}
		
		return ret;
	}

	private ComponentInformationFunctionNode createJoinNode(
			ComponentInformationFunctionNode left,
			ComponentInformationFunctionNode right) {
		ComponentInformationFunctionNode currentNode = new ComponentInformationJoinNode(left.information(),right.information());
		left.addChild(currentNode);
		right.addChild(currentNode);
		return currentNode;
	}

	private ComponentInformationFunctionNode createFilterNode(
			ComponentInformationFunctionNode ret,
			ComponentInformationFunction<Triple, Map<String, String>> currentFilter) {
		ComponentInformationFunctionNode currentNode = new ComponentInformationFunctionNode(
				currentFilter.information(), 
				ContextFunction.wrap("triple","binding",currentFilter)
		);
		ret.addChild(currentNode);
		currentNode.addParent(ret);
		return currentNode;
	}

}
