package org.openimaj.squall.orchestrate.greedy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.rdf.storm.topology.ReteTopologyTest;
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
import org.openimaj.squall.orchestrate.exception.PlanningException;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.squall.utils.OPSDisplayUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.function.Function;
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
		NamedNode<? extends IVFunction<Context, Context>> finalsys;
		try {
			finalsys = orchestrate(ret,sys);
		} catch (PlanningException e) {
			throw new Error(e); // TODO
		}
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

	private NamedNode<? extends IVFunction<Context,Context>> orchestrate(OrchestratedProductionSystem root,CompiledProductionSystem sys) throws PlanningException {
		
		
//		if( nocps && nojoinable && nopredicates && noconsequences) {
//			throw EmptyCPSError
//			Deal with empty cps in this function
//			deal with empty cps in the orchestrate function
//		}
		
//		if( nocps && nojoinable && predicates && noconsequences) {
//			throwing FloatingPredicatesError
//			Deal with floating predicates in this function
//			deal with floating predicates in the orchestrate function
//		}
		
//		if(nocps && nojoinable && predicates && consequence){
//			throwing FloatingPredicatesError
//			Deal with floating predicates in this function
//			deal with floating predicates in the orchestrate function
//		}
		
		NamedNode<? extends IVFunction<Context, Context>> combinedFilters;
		if (sys.getJoinComponents().size() > 0){
			combinedFilters = orchestrateJoinComponents(root,sys.getJoinComponents());
		} else {
			combinedFilters = null;
		}
		
		List<NamedNode<? extends IVFunction<Context, Context>>> joinedCPS = new ArrayList<NamedNode<? extends IVFunction<Context, Context>>>();
		for (CompiledProductionSystem cps : sys.getSystems()) {
			try{
				NamedNode<? extends IVFunction<Context, Context>> combined = orchestrate(root,cps);
				
				if(combinedFilters != null){ // join the sub systems to any filters
					combined = createJoinNode(root, combined,combinedFilters);
				}
				joinedCPS.add(combined);
			} catch (FloatingPredicatesPlanningException e) {
				throw new Error(e); // TODO
			} catch (EmptyCPSPlanningException e) {
				e.printStackTrace();
			} catch (PlanningException e) {
				throw new Error(e); // TODO
			}
		}
		if(joinedCPS.size() == 0 && combinedFilters != null){
			// There were no sub CPS to join with, just add the combined filters to the list, if one exists
			joinedCPS.add(combinedFilters);
		}
		
		// If there are predicates, check that the CPS produces some data (has some joinedCPSs and/or a combinedFilters)
		if (sys.getPredicates().size() > 0)
			if (joinedCPS.size() > 0){
				// If it does, orchestrate the predicates in a chain onto each of the joinedCPSs
				joinedCPS = orchestratePredicates(root, joinedCPS, sys.getPredicates());
			} else {
				// If it does NOT, throw a Floating Predicates Exception
				// (should be pretty lethal, as predicates are inherently incapable of handling raw semantic data)
				throw new FloatingPredicatesPlanningException("Found in CompiledProductionSystem: "+sys.toString());
			}
		
//		aggregations = orchestrateAggregations(joinedCPS,sys.getAggregations());
		if(sys.getConsequence() == null){
			// If there are subCPSs that have consequences...
			if (joinedCPS.size() > 0)
				// ... use an implicit consequence that is a simple passthrough function
				return orchestrateConsequences(root, joinedCPS,new PassThroughConsequence());
			else
				// ... otherwise, throw an error declaring the CompiledProductionSystem was empty.
				throw new EmptyCPSPlanningException();
		}
		return orchestrateConsequences(root, joinedCPS,sys.getConsequence());
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

	private List<NamedNode<?  extends IVFunction<Context, Context>>> orchestratePredicates(
			OrchestratedProductionSystem root,
			List<NamedNode<?  extends IVFunction<Context, Context>>> currentNodes,
			List<IVFunction<Context,Context>> list) {
		
		List<NamedNode<? extends IVFunction<Context, Context>>> newFinalNodes = new ArrayList<NamedNode<? extends IVFunction<Context,Context>>>();
		for (NamedNode<? extends IVFunction<Context, Context>> currentNode : currentNodes){
			for (IVFunction<Context, Context> pred : list) {
				NNIVFunction prednode = new NNIVFunction(
						root,
						nextPredicateName(),
						pred
				);
				currentNode.connect(new NamedStream("link"), prednode);
				currentNode = prednode;
			}
			newFinalNodes.add(currentNode);
		}
		return newFinalNodes;
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
					next = orchestrate(root, cps);
				} catch (FloatingPredicatesPlanningException e) {
					throw new Error(e); // TODO
				} catch (EmptyCPSPlanningException e) {
					next = null;
				} catch (PlanningException e) {
					throw new Error(e); // TODO
				}
			} else{
				// ignore unknown join comp
				throw new Error(); // TODO
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
				nTripleStream = ReteTopologyTest.class.getResourceAsStream("/test.rdfs");
			}
			
			@Override
			public void cleanup() { }
		};
		
		RIFEntailmentImportProfiles profs = new RIFOWLImportProfiles();
		RIFRuleSet rules = null;
		try {
			rules = profs.parse(
					new URI("http://www.w3.org/2005/rules/test/repository/tc/IRI_from_RDF_Literal/IRI_from_RDF_Literal-premise.rif"),
					new URI("http://www.w3.org/ns/entailment/Core")
				);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
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
				new RIFCoreRuleCompiler().compile(
						new RulesetLibsPair(
								rules, new ArrayList<RIFExternalFunctionLibrary>()
						)
				), op);
		
		OPSDisplayUtils.display(ops);
		
		
	}

}
