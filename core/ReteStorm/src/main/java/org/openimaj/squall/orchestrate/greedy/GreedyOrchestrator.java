package org.openimaj.squall.orchestrate.greedy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.rif.RIFRuleSet;
import org.openimaj.rif.contentHandler.RIFEntailmentImportProfiles;
import org.openimaj.rif.contentHandler.RIFOWLImportProfiles;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.JoinComponent;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.rif.RIFCoreRuleCompiler;
import org.openimaj.squall.compile.rif.RulesetLibsPair;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.functions.rif.RIFExternalFunctionLibrary;
import org.openimaj.squall.orchestrate.NNIVFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.squall.orchestrate.exception.EmptyCPSPlanningException;
import org.openimaj.squall.orchestrate.exception.FloatingPredicatesPlanningException;
import org.openimaj.squall.orchestrate.exception.MultiConsequenceSubCPSPlanningException;
import org.openimaj.squall.orchestrate.exception.PlanningException;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.squall.utils.OPSDisplayUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;
import org.xml.sax.SAXException;

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
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem sys, IOperation<Context> op) {
		OrchestratedProductionSystem ret = new OrchestratedProductionSystem();		
		ret.root = new ArrayList<NamedSourceNode>();
		orchestrateSources(sys,ret);
		List<NamedNode<? extends IVFunction<Context, Context>>> finalsys = new ArrayList<NamedNode<? extends IVFunction<Context,Context>>>();
		try {
			for (IndependentPair<NamedNode<? extends IVFunction<Context, Context>>, List<IVFunction<Context, Context>>> fs : orchestrate(ret,sys)){
				if (fs.getSecondObject().isEmpty()){
					finalsys.add(fs.getFirstObject());
				} else {
					throw new FloatingPredicatesPlanningException();
				}
			}
		} catch (PlanningException e) {
			throw new Error("Any PlanningException received at root is unexpected.",e);
		}
		if(finalsys.isEmpty()){
			orchestrateOperation(ret, op);
		} else {
			orchestrateOperation(ret, op, finalsys);
		}
		return ret;
	}

	private void orchestrateOperation(OrchestratedProductionSystem ret, IOperation<Context> op) {
		NamedNode<?> opNode = new NGNOperation(ret, "OPERATION", op);
		for (NamedNode<?> namedNode : ret.getLeaves()) {			
			namedNode.connect(new NamedStream("link"), opNode);
		}
	}

	private void orchestrateOperation(OrchestratedProductionSystem ret, IOperation<Context> op, List<NamedNode<? extends IVFunction<Context, Context>>> finalsys) {
		NamedNode<?> opNode = new NGNOperation(ret, "OPERATION", op);
		for (NamedNode<? extends IVFunction<Context, Context>> sys : finalsys){
			sys.connect(new NamedStream("link"), opNode);
		}
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

	private List<IndependentPair<NamedNode<? extends IVFunction<Context,Context>>, List<IVFunction<Context, Context>>>>
						orchestrate(OrchestratedProductionSystem root,CompiledProductionSystem sys) throws PlanningException {
		NamedNode<? extends IVFunction<Context, Context>> combinedFilters;
		if (sys.getJoinComponents().size() > 0){
			combinedFilters = orchestrateJoinComponents(root,sys.getJoinComponents());
		} else {
			combinedFilters = null;
		}
		
		List<IndependentPair<NamedNode<? extends IVFunction<Context,Context>>, List<IVFunction<Context, Context>>>>
			joinedCPSs = new ArrayList<IndependentPair<NamedNode<? extends IVFunction<Context,Context>>,List<IVFunction<Context,Context>>>>();
		for (CompiledProductionSystem cps : sys.getSystems()) {
			try {
				for (IndependentPair<NamedNode<? extends IVFunction<Context,Context>>, List<IVFunction<Context, Context>>>
						combinedCPS : orchestrate(root,cps)){
					NamedNode<? extends IVFunction<Context,Context>> newNN;
					if(combinedFilters != null){ // join the sub systems to any filters
						newNN = createJoinNode(root, combinedCPS.getFirstObject(),combinedFilters);
					}else{
						newNN = combinedCPS.getFirstObject();
					}
					
					List<IVFunction<Context,Context>> newPreds = combinedCPS.getSecondObject();
					if (sys.getPredicates().size() > 0)
						newPreds.addAll(sys.getPredicates());
					
					joinedCPSs.add(new IndependentPair<NamedNode<? extends IVFunction<Context,Context>>, List<IVFunction<Context,Context>>>(newNN, newPreds));
				}
			} catch (FloatingPredicatesPlanningException e) {
				throw new Error("Floating Predicates should not be a problem here.",e);
			} catch (EmptyCPSPlanningException e) {
				System.err.println("Empty CPSs are allowed at this point.");
				e.printStackTrace();
			} catch (MultiConsequenceSubCPSPlanningException e) {
				throw new Error("Multiple consequences should not be a problem here.",e);
			} catch (PlanningException e) {
				throw new Error("Generic or unknown PlanningException received.",e);
			}
		}
		
		if(joinedCPSs.size() == 0 && (combinedFilters != null || sys.getPredicates().size() > 0)){
			// There were no sub CPS to join with, just add the combined filters to the list, if one exists
			joinedCPSs.add(new IndependentPair<NamedNode<? extends IVFunction<Context,Context>>, List<IVFunction<Context,Context>>>(combinedFilters, sys.getPredicates()));
		}
		
//		aggregations = orchestrateAggregations(joinedCPS,sys.getAggregations());
		
		
		if(sys.getConsequences().isEmpty()){
			// If the CPS has no individual consequences, then return all tree roots so far produced with associated predicate groups.
			return joinedCPSs;
		}else{
			List<IndependentPair<NamedNode<? extends IVFunction<Context,Context>>,List<IVFunction<Context,Context>>>>
				completedCPSs = new ArrayList<IndependentPair<NamedNode<? extends IVFunction<Context, Context>>, List<IVFunction<Context, Context>>>>();
			
			// Loop through all the completed orchestrated systems (consequences capped onto the result of orchestrating predicates onto combinedFilters)
			// and create new Pairs of systems and predicates.
			for (NamedNode<? extends IVFunction<Context,Context>> consequencedRule : orchestrateConsequences(root,orchestratePredicates(root,joinedCPSs),sys.getConsequences())){
				List<IVFunction<Context,Context>> emptyPredicates = new ArrayList<IVFunction<Context,Context>>();
				completedCPSs.add(new IndependentPair<NamedNode<? extends IVFunction<Context,Context>>,List<IVFunction<Context,Context>>>(
									consequencedRule,
									emptyPredicates
								));
			}
			
			return completedCPSs;
		}
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

	private List<NamedNode<? extends IVFunction<Context,Context>>> orchestrateConsequences(
			OrchestratedProductionSystem root,
			List<NamedNode<? extends IVFunction<Context, Context>>> predicatedCombinedFilters,
			List<IVFunction<Context,Context>> functions) {
		List<NamedNode<? extends IVFunction<Context,Context>>> consequencesList = new ArrayList<NamedNode<? extends IVFunction<Context,Context>>>();
		for (IVFunction<Context, Context> function : functions){
			NNIVFunction consequenceNode = new NNIVFunction(
				root,
				nextConsequenceName(), 
				function
			);
			for (NamedNode<? extends IVFunction<Context, Context>> namedNode : predicatedCombinedFilters) {
				namedNode.connect(new NamedStream("link"), consequenceNode);
			}
			consequencesList.add(consequenceNode);
		}
		return consequencesList;
	}

	private String nextConsequenceName() {
		return String.format("CONSEQUENCE_%d",consequence++ );
	}

	private List<NamedNode<?  extends IVFunction<Context, Context>>> orchestratePredicates(
			OrchestratedProductionSystem root,
			List<IndependentPair<NamedNode<?  extends IVFunction<Context, Context>>, List<IVFunction<Context,Context>>>> cpss) {
		
		List<NamedNode<? extends IVFunction<Context, Context>>> newFinalNodes = new ArrayList<NamedNode<? extends IVFunction<Context,Context>>>();
		for (IndependentPair<NamedNode<? extends IVFunction<Context, Context>>, List<IVFunction<Context, Context>>> currentCPS : cpss){
			newFinalNodes.add(orchestratePredicates(root, currentCPS.getFirstObject(), currentCPS.getSecondObject()));
		}
		return newFinalNodes;
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
				CompiledProductionSystem cps = jc.getTypedComponent();
				try {
					List<IndependentPair<NamedNode<? extends IVFunction<Context,Context>>,List<IVFunction<Context,Context>>>> subCons = orchestrate(root, cps);
					if (subCons.size() == 1){
						IndependentPair<NamedNode<? extends IVFunction<Context,Context>>,List<IVFunction<Context,Context>>>
							temp = subCons.get(0);
						if (temp.getSecondObject().isEmpty()){
							next = temp.getFirstObject();
						} else {
							throw new FloatingPredicatesPlanningException();
						}
					}else{
						throw new MultiConsequenceSubCPSPlanningException();
					}
				} catch (EmptyCPSPlanningException e) {
					next = null;
				} catch (PlanningException e) {
					throw new Error("Any inconsistency other than Empty CPSs is fatal.",e);
				}
			} else{
				// ignore unknown join comp
				throw new Error("Unknown JoinComponent encounterred.");
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
	
	/**
	 * Draws an example {@link GreedyOrchestrator} from RIF rules
	 * @param args
	 */
	public static void main(String[] args) {
		ISource<Stream<Context>> tripleContextStream = new ISource<Stream<Context>>() {
			
			private InputStream nTripleStream;

			@Override
			public Stream<Context> apply(Stream<Context> in) {
				return apply();
			}
			
			@Override
			public Stream<Context> apply() {
				return new CollectionStream<Triple>(JenaUtils.readNTriples(nTripleStream))
				.map(new ContextWrapper("triple"));
//				return null;
			}
			
			@Override
			public void setup() { 
				nTripleStream = GreedyOrchestrator.class.getResourceAsStream("/test.rdfs");
			}
			
			@Override
			public void cleanup() { }
		};
		
		RIFEntailmentImportProfiles profs = new RIFEntailmentImportProfiles();
		RIFRuleSet rules = null;
		try {
			rules = profs.parse(
					new URI("http://users.ecs.soton.ac.uk/dm11g08/semantics/rif/ontology-rules/OWL2RL.rif"),
//					new URI("http://users.ecs.soton.ac.uk/dm11g08/semantics/rif/ontology-rules/OWL2RLSimpleRules.rif"),
//					new URI("http://users.ecs.soton.ac.uk/dm11g08/semantics/rif/ontology-rules/OWL2RLDatatypeRules.rif"),
//					new URI("http://users.ecs.soton.ac.uk/dm11g08/semantics/rif/ontology-rules/OWL2RLListRules.rif"),
					new URI("http://www.w3.org/ns/entailment/Core")
				);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		List<ISource<Stream<Context>>> sources = new ArrayList<ISource<Stream<Context>>>();
		sources.add(tripleContextStream);
		
		GreedyOrchestrator go = new GreedyOrchestrator();
		IOperation<Context> op = new IOperation<Context>() {
			
			@Override
			public void setup() { }
			
			@Override
			public void cleanup() { }
			
			@Override
			public void perform(Context object) { }
		};
		OrchestratedProductionSystem ops = go.orchestrate(
				new RIFCoreRuleCompiler().compile(rules), op);
		
		OPSDisplayUtils.display(ops);
		
		
	}

}
