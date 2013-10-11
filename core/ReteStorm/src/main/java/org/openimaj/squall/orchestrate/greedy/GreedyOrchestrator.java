package org.openimaj.squall.orchestrate.greedy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.ReteTopologyTest;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.jena.JenaRuleCompiler;
import org.openimaj.squall.compile.jena.SourceRulePair;
import org.openimaj.squall.compile.jena.TestJenaRuleCompiler;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.squall.utils.OPSDisplayUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.android.dx.command.grep.Grep;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

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
				root.root.add(new NamedSourceNode(root,nextSourceName(root), sourceS));
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
		combinedFilters = orchestratePredicates(root,combinedFilters,sys.getPredicates());
		
		List<NamedNode<? extends IVFunction<Context, Context>>> joinedCPS = new ArrayList<NamedNode<? extends IVFunction<Context, Context>>>();
		for (List<CompiledProductionSystem<Context,Context>> subsyslist : sys.getSystems()) {
			NamedNode<? extends IVFunction<Context, Context>> combined = null;
			for (CompiledProductionSystem<Context, Context> subsys : subsyslist) {
				NamedNode<? extends IVFunction<Context, Context>> next = orchestrate(root,subsys);
				if(combined == null){
					combined = next;
				} else{
					combined = createJoinNode(root, combined, next);
				}
			}
			if(combined!=null && combinedFilters != null){ // join the sub systems to any filters
				combined = createJoinNode(root, combined,combinedFilters);
			}
			joinedCPS.add(combined);
		}
		if(joinedCPS.size() == 0){
			// There were no sub CPS to join with, just add the combined filters to the list
			joinedCPS.add(combinedFilters);
		}
//		aggregations = orchestrateAggregations(joinedCPS,sys.getAggregations());
		return orchestrateConsequences(root, joinedCPS,sys.getConequences());
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
			OrchestratedProductionSystem root,
			List<NamedNode<? extends IVFunction<Context, Context>>> joinedCPS,
			IVFunction<Context,Context> function) {
		NamedIVFunctionNode consequenceNode = new NamedIVFunctionNode(
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
		return String.format("CONSEQUENCE_%d",consequence++ );
	}

	private NamedNode<?  extends IVFunction<Context, Context>> orchestratePredicates(
			OrchestratedProductionSystem root,
			NamedNode<?  extends IVFunction<Context, Context>> currentNode,
			List<IVFunction<Context,Context>> list) {
		
		for (IVFunction<Context, Context> pred : list) {
			NamedIVFunctionNode prednode = new NamedIVFunctionNode(
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
				ret = createJoinNode(root, ret, next);
			}
		}
		return ret ;
	}

	private NamedNode<? extends IVFunction<Context, Context>> createJoinNode(
			OrchestratedProductionSystem root,
			NamedNode<? extends IVFunction<Context, Context>> left,
			NamedNode<? extends IVFunction<Context, Context>> right) {
		
		NamedJoinNode joined = new NamedJoinNode(root,nextJoinName(), left, right);
		return joined;
	}

	private String nextJoinName() {
		return String.format("JOIN_%d",join ++ );
	}

	private NamedIVFunctionNode createFilterNode(
			OrchestratedProductionSystem root,
			IVFunction<Context,Context> filterFunc) {
		NamedIVFunctionNode currentNode = new NamedIVFunctionNode(
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
		return String.format("FILTER_%d",filter ++);
	}
	
	public static void main(String[] args) {
		InputStream nTripleStream = ReteTopologyTest.class.getResourceAsStream("/test.rdfs");
		InputStream ruleStream = TestJenaRuleCompiler.class.getResourceAsStream("/test.rules");
		
		Stream<Context> tripleContextStream = 
			new CollectionStream<Triple>(JenaUtils.readNTriples(nTripleStream))
			.map(new ContextWrapper("triple"));
		
		List<Rule> rules = JenaUtils.readRules(ruleStream);
		
		GreedyOrchestrator go = new GreedyOrchestrator();
		OrchestratedProductionSystem ops = go.orchestrate(new JenaRuleCompiler().compile(SourceRulePair.simplePair(tripleContextStream, rules)));
		
		OPSDisplayUtils.display(ops);
		
		
	}

}
