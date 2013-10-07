package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.VariableFunction;
import org.openimaj.squall.orchestrate.NamedFunctionNode;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

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
		ret.root = new ArrayList<NamedSourceNode>();
		orchestrate(ret,sys);
		return ret;
	}

	private void orchestrate(OrchestratedProductionSystem root,CompiledProductionSystem<Triple,Triple> sys) {
		NamedNode<?> currentNode = null;
		
		if(sys.getSources().size()>0){
			for (Stream<Triple> sourceS: sys.getSources()) {				
				root.root.add(new NamedSourceNode(nextSourceName(root), sourceS.map(new Function<Triple,Context>(){

					@Override
					public Context apply(Triple in) {
						Context ret = new Context();
						ret.put("triple", in);
						return ret;
					}
					
				})));
			}
		}
		// FIXME: The Sub systems must be joined!
		for (CompiledProductionSystem<Triple,Triple> subsys : sys.getSystems()) {
			orchestrate(root,subsys);
		}
		currentNode = orchestrateFilters(root,currentNode,sys.getFilters());
		currentNode = orchestratePredicates(currentNode,sys.getPredicates());
		orchestrateConsequences(currentNode,sys.getConequences());
	}


	private String nextSourceName(OrchestratedProductionSystem ret) {
		return "source_" + ret.root.size();
	}

	private void orchestrateConsequences(
			NamedNode<?> currentNode,
			List<Function<Map<String, String>, Triple>> conequences) {
		for (Function<Map<String, String>, ?> function : conequences) {
			NamedFunctionNode consequenceNode = new NamedFunctionNode(
				nextConsequenceName(), 
				ContextFunction.wrap("binding","consequence",function)
			);
			
		}
	}

	private String nextConsequenceName() {
		return String.format("CONSEQUENCE_%d",consequence++ );
	}

	private NamedNode<?> orchestratePredicates(
			NamedNode<?> currentNode,
			List<VariableFunction<Map<String, String>,Map<String, String>>> predicates) {
		
		for (VariableFunction<Map<String, String>, Map<String, String>> pred : predicates) {
			NamedVarFunctionNode prednode = new NamedVarFunctionNode(
				nextPredicateName(),
				ContextVariableFunction.wrap("binding","binding",pred)
			);
			
			currentNode = prednode;
		}
		return currentNode;
	}

	private String nextPredicateName() {
		return String.format("PREDICATE_%d",predicate ++ );
	}

	private NamedNode<?> orchestrateFilters(
			OrchestratedProductionSystem root, 
			NamedNode<?> ret,
			List<VariableFunction<Triple, Map<String, String>>> list
	) {
		
		return ret;
	}

	private NamedNode<?> createJoinNode(
			NamedVarFunctionNode left,
			NamedVarFunctionNode right) {
				return right;
	}

	private String nextJoinName() {
		return String.format("JOIN_%d",join ++ );
	}

	private NamedVarFunctionNode createFilterNode(
			OrchestratedProductionSystem root,
			VariableFunction<Triple, Map<String, String>> currentFilter) {
		NamedVarFunctionNode currentNode = new NamedVarFunctionNode(
				nextFilterName(), 
				ContextVariableFunction.wrap("triple","binding",currentFilter)
		);
		return currentNode;
	}

	private String nextFilterName() {
		return String.format("FILTER_%d",filter ++);
	}

}
