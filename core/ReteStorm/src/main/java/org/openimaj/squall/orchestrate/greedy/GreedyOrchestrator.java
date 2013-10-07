package org.openimaj.squall.orchestrate.greedy;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.VariableFunction;
import org.openimaj.squall.orchestrate.NamedFunctionNode;
import org.openimaj.squall.orchestrate.NamedStream;
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
public class GreedyOrchestrator implements Orchestrator<Triple,Triple>{
	
	private int consequence = 0;
	private int predicate = 0;
	private int filter = 0;
	private int join = 0;

	@Override
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem<Triple,Triple> sys) {
		OrchestratedProductionSystem ret = new OrchestratedProductionSystem();
		ret.root = new NamedFunctionNode("root", null);
		orchestrate(ret.root,sys);
		return ret;
	}

	private NamedVarFunctionNode orchestrate(NamedFunctionNode root,CompiledProductionSystem<Triple,Triple> sys) {
		NamedVarFunctionNode currentNode = null;
		
		// FIXME: Sources should produce a differenet kind of NamedFunctionNode which is a stream, not a function
//		if(sys.getSources().size()>0){
//			new NamedFunctionNode(
//				nextSourceName(), 
//				ContextFunction.wrap(null,"triple", function)
//			);
//		}
		// FIXME: The Sub systems must be joined!
		for (CompiledProductionSystem<Triple,Triple> subsys : sys.getSystems()) {
			currentNode = orchestrate(root,subsys);
		}
		currentNode = orchestrateFilters(currentNode,root,sys.getFilters());
		currentNode = orchestratePredicates(currentNode,sys.getPredicates());
		orchestrateConsequences(currentNode,sys.getConequences());
		return currentNode;
	}


	int sources = 0;
	private String nextSourceName() {
		return "source_" + sources ++;
	}

	private void orchestrateConsequences(
			NamedFunctionNode ret,
			List<Function<Map<String, String>, Triple>> conequences) {
		for (Function<Map<String, String>, ?> function : conequences) {
			NamedFunctionNode consequenceNode = new NamedFunctionNode(
				nextConsequenceName(), 
				ContextFunction.wrap("binding","consequence",function)
			);
			ret.connect(new NamedStream<NamedFunctionNode>("link", ret, consequenceNode), consequenceNode);
		}
	}

	private String nextConsequenceName() {
		return String.format("CONSEQUENCE_%d",consequence++ );
	}

	private NamedVarFunctionNode orchestratePredicates(
			NamedVarFunctionNode ret,
			List<VariableFunction<Map<String, String>,Map<String, String>>> predicates) {
		
		for (VariableFunction<Map<String, String>, Map<String, String>> pred : predicates) {
			NamedVarFunctionNode prednode = new NamedVarFunctionNode(
					nextPredicateName(),
					ContextVariableFunction.wrap("binding","binding",pred)
				);
			ret.connect(new NamedStream<NamedFunctionNode>("link", ret, prednode),prednode);
			ret = prednode;
		}
		return ret;
	}

	private String nextPredicateName() {
		return String.format("PREDICATE_%d",predicate ++ );
	}

	private NamedVarFunctionNode orchestrateFilters(
			NamedVarFunctionNode ret,
			NamedFunctionNode root, 
			List<VariableFunction<Triple, Map<String, String>>> list
	) {
		
		// Always add filters to the root directly, but join them to each other
		Iterator<VariableFunction<Triple, Map<String, String>>> filterIter = list.iterator();
		if(ret == null) // Nothing else to join with yet, add the first filter 
		{
			ret = createFilterNode(root, filterIter.next());
		}
		while(filterIter.hasNext()){
			NamedVarFunctionNode nextNode = createFilterNode(root, filterIter.next()); 
			ret = createJoinNode(ret,nextNode);
		}
		
		return ret;
	}

	private NamedVarFunctionNode createJoinNode(
			NamedVarFunctionNode left,
			NamedVarFunctionNode right) {
		NamedJoinNode currentNode = new NamedJoinNode(
				nextJoinName(),
				left,
				right
		);
		left.connect(currentNode.leftNamedStream(), currentNode);
		right.connect(currentNode.rightNamedStream(), currentNode);
		return currentNode;
	}

	private String nextJoinName() {
		return String.format("JOIN_%d",join ++ );
	}

	private NamedVarFunctionNode createFilterNode(
			NamedFunctionNode ret,
			VariableFunction<Triple, Map<String, String>> currentFilter) {
		NamedVarFunctionNode currentNode = new NamedVarFunctionNode(
				nextFilterName(), 
				ContextVariableFunction.wrap("triple","binding",currentFilter)
		);
		ret.connect(new NamedStream<NamedFunctionNode>("link", ret, currentNode),currentNode);
		return currentNode;
	}

	private String nextFilterName() {
		return String.format("FILTER_%d",filter ++);
	}

}
