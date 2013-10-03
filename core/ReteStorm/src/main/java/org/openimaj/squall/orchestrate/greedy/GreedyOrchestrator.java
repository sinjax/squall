package org.openimaj.squall.orchestrate.greedy;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.ComponentInformationFunction;
import org.openimaj.squall.compile.data.ComponentInformationPredicate;
import org.openimaj.squall.orchestrate.DAGNode;
import org.openimaj.squall.orchestrate.NamedFunctionNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.util.function.Function;

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

	private int consequence = 0;
	private int predicate = 0;
	private int filter = 0;
	private int join = 0;

	@Override
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem sys) {
		OrchestratedProductionSystem ret = new OrchestratedProductionSystem();
		ret.root = new NamedFunctionNode("root", sys.information(), null);
		orchestrate(ret.root,sys);
		return ret;
	}

	private NamedFunctionNode orchestrate(NamedFunctionNode root,CompiledProductionSystem sys) {
		NamedFunctionNode currentNode = null;
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
			NamedFunctionNode ret,
			List<Function<Map<String, String>, ?>> conequences) {
		for (Function<Map<String, String>, ?> function : conequences) {
			NamedFunctionNode consequenceNode = new NamedFunctionNode(
				nextConsequenceName(),
				null, 
				ContextFunction.wrap("binding","consequence",function)
			);
			DAGNode.link(ret, consequenceNode);
		}
	}

	private String nextConsequenceName() {
		return String.format("CONSEQUENCE_%d",consequence++ );
	}

	private NamedFunctionNode orchestratePredicates(
			NamedFunctionNode ret,
			List<ComponentInformationPredicate<Map<String, String>>> predicates) {
		
		for (ComponentInformationPredicate<Map<String,String>> pred : predicates) {
			NamedFunctionNode prednode = new NamedFunctionNode(
					nextPredicateName(),
					pred.information(), 
					ContextFunction.wrap("binding","binding",new FilterFunction<Map<String,String>>(pred))
				);
			DAGNode.link(ret, prednode);
			ret = prednode;
		}
		return ret;
	}

	private String nextPredicateName() {
		return String.format("PREDICATE_%d",predicate ++ );
	}

	private NamedFunctionNode orchestrateFilters(
			NamedFunctionNode ret,
			NamedFunctionNode root, 
			List<ComponentInformationFunction<Triple, Map<String, String>>> list
	) {
		
		// Always add filters to the root directly, but join them to each other
		
		Iterator<ComponentInformationFunction<Triple, Map<String, String>>> filterIter = list.iterator();
		if(ret == null) // Nothing else to join with yet, add the first filter 
		{
			ret = createFilterNode(root, filterIter.next());
		}
		while(filterIter.hasNext()){
			NamedFunctionNode nextNode = createFilterNode(root, filterIter.next()); 
			ret = createJoinNode(ret,nextNode);
		}
		
		return ret;
	}

	private NamedFunctionNode createJoinNode(
			NamedFunctionNode left,
			NamedFunctionNode right) {
		NamedFunctionNode currentNode = new NamedJoinNode(
				nextJoinName(),
				left.information(),
				right.information()
		);
		DAGNode.link(left, currentNode);
		DAGNode.link(right, currentNode);
		return currentNode;
	}

	private String nextJoinName() {
		return String.format("JOIN_%d",join ++ );
	}

	private NamedFunctionNode createFilterNode(
			NamedFunctionNode ret,
			ComponentInformationFunction<Triple, Map<String, String>> currentFilter) {
		NamedFunctionNode currentNode = new NamedFunctionNode(
				nextFilterName(),
				currentFilter.information(), 
				ContextFunction.wrap("triple","binding",currentFilter)
		);
		DAGNode.link(ret, currentNode);
		return currentNode;
	}

	private String nextFilterName() {
		return String.format("FILTER_%d",filter ++);
	}

}
