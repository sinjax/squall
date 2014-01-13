package org.openimaj.squall.orchestrate.rete;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openimaj.rifcore.RIFRuleSet;
import org.openimaj.rifcore.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.JoinComponent;
import org.openimaj.squall.compile.OptionalProductionSystems;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.rif.RIFCoreRuleCompiler;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.orchestrate.CPSResult;
import org.openimaj.squall.orchestrate.CompleteCPSResult;
import org.openimaj.squall.orchestrate.NNIVFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.squall.orchestrate.PartialCPSResult;
import org.openimaj.squall.orchestrate.ReentrantNNIFunction;
import org.openimaj.squall.orchestrate.WindowInformation;
import org.openimaj.squall.orchestrate.exception.CompleteCPSPlanningException;
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
	private WindowInformation wi;
	
	/**
	 * @param capacity the capacity of the window
	 * @param duration the duration of time
	 * @param time the time unit
	 */
	public GreedyOrchestrator(int capacity, long duration, TimeUnit time) {
		this.wi = new WindowInformation(capacity, duration, time);
	}
	
	/**
	 * 
	 */
	public GreedyOrchestrator() {
		this(1000,1,TimeUnit.MINUTES);
	}

	@Override
	public OrchestratedProductionSystem orchestrate(CompiledProductionSystem sys, IOperation<Context> op) {
		OrchestratedProductionSystem ret = new OrchestratedProductionSystem();		
		
		orchestrateSources(sys,ret);
		List<NamedNode<? extends IVFunction<Context, Context>>> finalsys = new ArrayList<NamedNode<? extends IVFunction<Context,Context>>>();
		try {
			CPSResult result = orchestrate(ret,sys);
			finalsys = result.getResults();
		} catch (PlanningException e) {
			throw new Error("Any PlanningException received at root is unexpected.",e);
		}
		if(finalsys.isEmpty()){
			orchestrateOperation(ret, op);
		} else {
			orchestrateOperation(ret, op, finalsys);
		}
		
		// Also connect all reentrant consequences to all the filters
		if(ret.reentrant != null) orchestrateReentrateConsequences(ret);
		
		return ret;
	}

	private void orchestrateReentrateConsequences(OrchestratedProductionSystem ret) {
		// The nodes which must be connected to reentrant consequences are those which are connected to any sources
		Set<NamedNode<?>> filters = new HashSet<NamedNode<?>>();
		for (NamedSourceNode source : ret.root) {
			for (NamedNode<?> namedNode : source.children()) {
				filters.add(namedNode);
			}
		}
		
//		for (NamedNode<?> filt : filters) {
//			
//			ret.reentrant.connect(new NamedStream("reentrantstream"), filt);
//		}
		
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

	protected void orchestrateSources(
			CompiledProductionSystem sys,
			OrchestratedProductionSystem root) {
		if(sys.getStreamSources().size()>0){
			for (ISource<Stream<Context>> sourceS: sys.getStreamSources()) {				
				root.root.add(new NamedSourceNode(root,nextSourceName(root), sourceS));
			}
		}
		for (OptionalProductionSystems opss : sys.getSystems()) {
			for (CompiledProductionSystem cps: opss) {
				orchestrateSources(cps, root);
			}
		}
	}

	/**
	 * This is the most complex method of the GreedyOrchestrator, as it has to handle UNIONs and OPTIONALs (Or(X,Y) and Or(X,true), respectively),
	 * with potentially many interacting in a given CPS, and each capable of holding both triple patterns and predicates (e.g. Filters and Functors).
	 * 
	 * The simplest way to achieve this in a semantically accurate way is to explicitly enumerate each distinct processing option implied by the
	 * current CPS; this involves processing each set of options (each UNION or OPTIONAL, Or(X,Y) or Or(X,true)) against the elicited processing
	 * options produced so far, then replacing the old options with the new ones.
	 * 
	 * NB: Or(X) is equivalent to Or(X,X) or {X} UNION {X}, which is equivalent to X.
	 * 
	 * Example:
	 * Select * where {
	 *    T1
	 *    {Select * where {
	 *    	T2
	 *    }
	 *    }UNION{
	 *    	T3
	 *    }
	 *    OPTIONAL {
	 *    	T4
	 *    }
	 * }
	 * 
	 * Start with T1 as the combined filters at this level, putting T1 into the list of processing options:
	 * 		options = [[T1]]
	 * Process the UNION's two optional CPSs and join their filters with each of the current processing options as new distinct options:
	 * 		options = [[T1, T2], [T1, T3]]
	 * Process the OPTIONAL's two optional CPSs (as OPTIONAL {T4} === {T4} UNION {}) and join their filters with each of the current processing
	 * options as new distinct options:
	 * 		options = [[T1, T2, T4], [T1, T2], [T1, T3, T4], [T1, T3]]
	 * 
	 * NB: Predicates are handled the same way and in parallel, with joined filters always bound to their relevant predicates in independent
	 * pairs, making the "options" above appear as below if the current CPS and the OPTIONAL CPS both contain FILTER clauses, labelled P1 and P2
	 * respectively:
	 * 		options = [[T1, T2, T4 : P1, P4], [T1, T2 : P1], ... ]
	 * @param root
	 * @param sys
	 * @return
	 * @throws PlanningException
	 */
	private CPSResult orchestrate(OrchestratedProductionSystem root,CompiledProductionSystem sys) throws PlanningException {
		// Create the list of paired predicates and joined filters, i.e. processing options
		PartialCPSResult joinedCPSs = new PartialCPSResult();
		// Create the list of fully processed rules resulting from this point.
		CompleteCPSResult unionedRules = new CompleteCPSResult();
		// Add a new pairing of predicates and joined filters to the list of processing options representing the lone option represnted by the current CPS
		// without optional subCPSs.
		joinedCPSs.add(
				sys.getJoinComponents(),
				sys.getPredicates()
		);
		
		// Then, for each set of options expressed immediately in the current CPS ...
		for (OptionalProductionSystems opss : sys.getSystems()) {
			// ... create a list of new processing options for the current CPS.
			PartialCPSResult newJoinedCPSs = new PartialCPSResult();
			// ... for each option try to ...
			for (CompiledProductionSystem cps : opss){
				CPSResult subResult = orchestrate(root,cps);
				try {
					// ... orchestrate the subCPS and then for each resulting processing option for that subCPS ...
					for (IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>> combinedCPS : subResult.getProcessingOptions()){
						// ... for each processing option determined prior to the processing of this set of options (UNION, Or(), etc) ...
						for (IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>>
								jCPS : joinedCPSs.getProcessingOptions()){
							// ... construct the new list of join components for this new option from those of the subCPS's current processing option
							// and those of the current prior processing option for the current CPS.
							List<JoinComponent<?>> newJoinComps = new ArrayList<JoinComponent<?>>();
								newJoinComps.addAll(combinedCPS.getFirstObject());
								newJoinComps.addAll(jCPS.getFirstObject());
							// ... construct the new list of predicates for this new option from the predicates of the subCPS's current processing option
							// and those of the current prior processing option for the current CPS.
							List<IVFunction<Context,Context>> newPreds = new ArrayList<IVFunction<Context, Context>>(); 
								newPreds.addAll(combinedCPS.getSecondObject());
								newPreds.addAll(jCPS.getSecondObject());
							// ... add the combinations of join components and predicates of the two CPS processing options to the list of new, combined
							// processing options
							newJoinedCPSs.add(newJoinComps, newPreds);
						}
					}
				} catch (CompleteCPSPlanningException e){
					unionedRules.addAll(subResult.getResults());
				}
			}
			
			joinedCPSs = newJoinedCPSs;
		}
		
//		aggregations = orchestrateAggregations(joinedCPS,sys.getAggregations());
		
		
		if(sys.getConsequences().isEmpty()){
			if (unionedRules.isEmpty()){
				// If the CPS has no individual consequences, then return all tree roots so far produced with associated predicate groups.
				return joinedCPSs;
			} else if (joinedCPSs.getProcessingOptions().isEmpty()) {
				return unionedRules;
			} else {
				for (IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>> jCPS : joinedCPSs.getProcessingOptions()) {
					if (!(jCPS.firstObject().isEmpty() && jCPS.secondObject().isEmpty())){
						throw new UnsupportedOperationException("Cannot interpret a subCPS with a consequence being optionally directly joined with JoinComponents."
																+ "Translate elements such as sub-queries into an optional CPS containing the sub-query CPS as a join component.");
					}
				}
				return unionedRules;
			}
		}else{
			CompleteCPSResult consequences = orchestrateConsequences(root,joinedCPSs, sys.getConsequences());
			unionedRules.addAll(consequences);
			if(sys.isReentrant()){
				orchestrateReentrantConsequences(root,consequences);
			}
			
			return unionedRules;
		}
	}


	private void orchestrateReentrantConsequences(OrchestratedProductionSystem root, CompleteCPSResult consequences) {
		for (NamedNode<? extends IVFunction<Context, Context>> namedNode : consequences) {
			Set<NamedNode<?>> filters = new HashSet<NamedNode<?>>();
			for (NamedSourceNode source : root.root) {
				for (NamedNode<?> filterNN : source.children()) {
					filters.add(filterNN);
				}
			}
			for (NamedNode<?> filter : filters){
				
			}
			namedNode.connect(
				new NamedStream("link"), 
				root.reentrant
			);
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

	protected String nextSourceName(OrchestratedProductionSystem ret) {
		return "source_" + ret.root.size();
	}

	private CompleteCPSResult orchestrateConsequences(
			OrchestratedProductionSystem root,
			PartialCPSResult partialCPS,
			List<IVFunction<Context, Context>> functions) {
		CompleteCPSResult consequencesList = new CompleteCPSResult();
		List<NamedNode<? extends IVFunction<Context, Context>>> bodies = orchestratePredicates(root, partialCPS);
		for (IVFunction<Context, Context> function : functions){
			NNIVFunction consequenceNode = new NNIVFunction(
				root,
				nextConsequenceName(), 
				function
			);
			for (NamedNode<? extends IVFunction<Context, Context>> body : bodies) {
				body.connect(new NamedStream("link"), consequenceNode);
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
			PartialCPSResult partialCPS) {
		
		List<NamedNode<? extends IVFunction<Context, Context>>> newFinalNodes = new ArrayList<NamedNode<? extends IVFunction<Context,Context>>>();
		try {
			for (IndependentPair<List<JoinComponent<?>>, List<IVFunction<Context, Context>>> currentCPS : partialCPS.getProcessingOptions()){
				newFinalNodes.add(orchestratePredicates(root, orchestrateJoinComponents(root, currentCPS.getFirstObject()), currentCPS.getSecondObject()));
			}
		} catch (CompleteCPSPlanningException e) {
			throw new Error("Should never get here", e);
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
					CPSResult results = orchestrate(root, cps);
					if (results.getResults().size() == 1)
						next = results.getResults().get(0);
					else
						throw new MultiConsequenceSubCPSPlanningException("Sub CPS returned multiple consequences, only one can be processed.");
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
		
		NGNJoin joined = new NGNJoin(root,nextJoinName(), left, right, this.wi);
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
