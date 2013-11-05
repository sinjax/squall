package org.openimaj.squall.orchestrate.greedy;


import java.util.ArrayList;
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
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem sys, IOperation<Context> op) {
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

	private void orchestrateSources(
			CompiledProductionSystem sys,
			OrchestratedProductionSystem root) {
		if(sys.getSources().size()>0){
			for (ISource<Stream<Context>> sourceS: sys.getSources()) {				
				root.root.add(new NamedSourceNode(root,nextSourceName(root), sourceS));
			}
		}
		for (CompiledProductionSystem cps: sys.getSystems()) {
			orchestrateSources(cps, root);
		}
	}

	private NamedNode<? extends IVFunction<Context,Context>> orchestrate(OrchestratedProductionSystem root,CompiledProductionSystem sys) {
		NamedNode<? extends IVFunction<Context, Context>> combinedFilters = orchestrateJoinComponents(root,sys.getJoinComponents());
		combinedFilters = orchestratePredicates(root,combinedFilters,sys.getPredicates());
		
		List<NamedNode<? extends IVFunction<Context, Context>>> joinedCPS = new ArrayList<NamedNode<? extends IVFunction<Context, Context>>>();
		for (CompiledProductionSystem cps : sys.getSystems()) {
			NamedNode<? extends IVFunction<Context, Context>> combined = orchestrate(root,cps);
			if(combined == null){
				throw new RuntimeException("No consequence of or'ed "); 
			}
			if(combinedFilters != null){ // join the sub systems to any filters
				combined = createJoinNode(root, combined,combinedFilters);
			}
			joinedCPS.add(combined);
		}
		if(joinedCPS.size() == 0){
			// There were no sub CPS to join with, just add the combined filters to the list
			joinedCPS.add(combinedFilters);
		}
//		aggregations = orchestrateAggregations(joinedCPS,sys.getAggregations());
		if(sys.getConequences() == null){ return null; }
		return orchestrateConsequences(root, joinedCPS,sys.getConequences());
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
		return String.format("CONSEQUENCE_%d",consequence++ );
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
		return String.format("PREDICATE_%d",predicate ++ );
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
				// If the sub compiled production system returns null, i.e. it has no consequence, 
				// this CPS might not be joined correctly
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

	private NamedNode<? extends IVFunction<Context, Context>> createJoinNode(
			OrchestratedProductionSystem root,
			NamedNode<? extends IVFunction<Context, Context>> left,
			NamedNode<? extends IVFunction<Context, Context>> right) {
		
		NGNJoin joined = new NGNJoin(root,nextJoinName(), left, right);
		return joined;
	}

	private String nextJoinName() {
		return String.format("JOIN_%d",join ++ );
	}

	private NNIVFunction createFilterNode(
			OrchestratedProductionSystem root,
			IVFunction<Context,Context> filterFunc) {
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
		return String.format("FILTER_%d",filter ++);
	}
	
//	/**
//	 * Draws an example {@link GreedyOrchestrator} 
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		InputStream ruleStream = GreedyOrchestrator.class.getResourceAsStream("/test.rules");
//		
//		ISource<Stream<Context>> tripleContextStream = new ISource<Stream<Context>>() {
//			
//			private InputStream nTripleStream;
//
//			@Override
//			public Stream<Context> apply(Stream<Context> in) {
//				return apply();
//			}
//			
//			@Override
//			public Stream<Context> apply() {
//				new CollectionStream<Triple>(JenaUtils.readNTriples(nTripleStream))
//				.map(new ContextWrapper("triple"));
//				return null;
//			}
//			
//			@Override
//			public void setup() { 
//				nTripleStream = ReteTopologyTest.class.getResourceAsStream("/test.rdfs");
//			}
//			
//			@Override
//			public void cleanup() { }
//		};
//		
//		List<Rule> rules = JenaUtils.readRules(ruleStream);
//		
//		GreedyOrchestrator go = new GreedyOrchestrator();
//		IOperation<Context> op = new IOperation<Context>() {
//			
//			@Override
//			public void setup() { }
//			
//			@Override
//			public void cleanup() { }
//			
//			@Override
//			public void perform(Context object) { }
//		};
//		OrchestratedProductionSystem ops = go.orchestrate(
//				new JenaRuleCompiler().compile(
//						SourceRulePair.simplePair(
//								tripleContextStream, rules
//						)
//				), op);
//		
//		OPSDisplayUtils.display(ops);
//		
//		
//	}

}
