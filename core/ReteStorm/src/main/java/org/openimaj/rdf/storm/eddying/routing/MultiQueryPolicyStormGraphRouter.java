package org.openimaj.rdf.storm.eddying.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.stems.StormSteMQueue;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;
import org.openimaj.util.pair.IndependentPair;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class MultiQueryPolicyStormGraphRouter extends StormGraphRouter {

	private static final long serialVersionUID = 4342744138230718341L;
	protected final static Logger logger = Logger.getLogger(MultiQueryPolicyStormGraphRouter.class);
	protected static int[] FACTORIALS = {0,1,2,6,24,120,720};
	

	private String[] queries;
	
	/**
	 * @param q
	 */
	public MultiQueryPolicyStormGraphRouter(String[] q){
		this.queries = q;
	}
	
	private int[] varCount;
	private List<TriplePattern>[] patterns;
	private Map<TripleMatch,Integer> stemStats;
	private Map<TripleMatch,Integer> stemRefs;
	
	protected void prepare(){
		stemStats = new HashMap<TripleMatch,Integer>();
		stemRefs = new HashMap<TripleMatch,Integer>();
		for (int i = 0; i < queries.length; i++){
			Rule rule = Rule.parseRule(queries[i]);
			varCount[i] = rule.getNumVars();
			try{
				int count = 0;
				patterns[i] = Arrays.asList((TriplePattern[]) rule.getBody());
				for (TriplePattern tp : patterns[i]){
					stemStats.put(tp.asTripleMatch(), count++);
					if (stemRefs.containsKey(tp.asTripleMatch()))
						stemRefs.put(tp.asTripleMatch(), stemRefs.get(tp.asTripleMatch()) + 1);
					else
						stemRefs.put(tp.asTripleMatch(), 1);
				}
			} catch (ClassCastException e){}
		}
	}
	
	@Override
	protected long routingTimestamp(long stamp1, long stamp2){
		return stamp1 > stamp2 ? stamp1 : -1;
	}
	
	private Node[] setEnvByVarNode(Node[] env, Node var, Node val){
		env[((Node_RuleVariable) var).getIndex()] = val;
		return env;
	}

	@Override
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
						   long timestamp) {
		Map<TripleMatch,List<Integer>> possibleProbeRefs = new HashMap<TripleMatch,List<Integer>>();
		Map<TripleMatch,TripleMatch> possibleProbeSteMs = new HashMap<TripleMatch,TripleMatch>();
		
		List<IndependentPair<List<TriplePattern>,Node[]>> previousSSQs = null;
		List<IndependentPair<List<TriplePattern>,Node[]>> satisfiedSubQueries = null;
		
queryLoop:
		for (int patternNumber = 0; patternNumber < patterns.length; patternNumber++){
			List<TriplePattern> pattern = patterns[patternNumber];
			// For each triple in the partial graph (the "filter nodes" of the inverse Rete network)...
			ExtendedIterator<Triple> triples = g.find(null,null,null);
graphLoop:
			for (Triple t = triples.next(); triples.hasNext(); t = triples.next()){
				// ... create a list of sub-queries to be populated with the sub-queries satisfied by the new triple combined with those previously tested,
				// then loop through all triple patterns in the current query...
				satisfiedSubQueries = new ArrayList<IndependentPair<List<TriplePattern>,Node[]>>();
patternLoop:
				for (TriplePattern current : pattern)
					// ... checking if the filter triple matches the current triple pattern, irrespective of intra-triple pattern joins.
					if ((current.getSubject().isVariable() || current.getSubject().sameValueAs(t.getSubject()))
							&& (current.getPredicate().isVariable() || current.getPredicate().sameValueAs(t.getPredicate()))
							&& (current.getObject().isVariable() || current.getObject().sameValueAs(t.getObject()))){
						// If the triple matches, then create a new environment for the node, then...
						Node[] env = new Node[varCount[patternNumber]];
						// ... populate it, whilst simultaneously checking for intra-triple pattern joins.
						if (current.getSubject().isVariable()){
							// Found a new variable, bind it in the environment.
							setEnvByVarNode(env, current.getSubject(), t.getSubject());
							
							if (current.getPredicate().isVariable()){
								if (((Node_RuleVariable) current.getSubject()).getIndex() != ((Node_RuleVariable) current.getPredicate()).getIndex())
									// Found a new variable, bind it in the environment.
									setEnvByVarNode(env, current.getPredicate(), t.getPredicate());
								else if (!t.getSubject().sameValueAs(t.getPredicate()))
									// Failed on intra-triple pattern join, stop processing of this triple pattern. 
									continue patternLoop;
								
								if (current.getObject().isVariable()){
									if (((Node_RuleVariable) current.getPredicate()).getIndex() != ((Node_RuleVariable) current.getObject()).getIndex())
										// Found a new variable, bind it in the environment.
										setEnvByVarNode(env, current.getObject(), t.getObject());
									else if (!t.getPredicate().sameValueAs(t.getObject()))
										// Failed on intra-triple pattern join, stop processing of this triple pattern.
										continue patternLoop;
								}
								
							}else if (current.getObject().isVariable()){
								if (((Node_RuleVariable) current.getSubject()).getIndex() != ((Node_RuleVariable) current.getObject()).getIndex())
									// Found a new variable, bind it in the environment.
									setEnvByVarNode(env, current.getObject(), t.getObject());
								else if (!t.getSubject().sameValueAs(t.getObject()))
									// Failed on intra-triple pattern join, stop processing of this triple pattern.
									continue patternLoop;
							}
							
						}else if (current.getPredicate().isVariable()){
							// Found a new variable, bind it in the environment.
							setEnvByVarNode(env, current.getPredicate(), t.getPredicate());
							
							if (current.getObject().isVariable()){
								if (((Node_RuleVariable) current.getPredicate()).getIndex() != ((Node_RuleVariable) current.getObject()).getIndex())
									// Found a new variable, bind it in the environment.
									setEnvByVarNode(env, current.getObject(), t.getObject());
								else if (!t.getPredicate().sameValueAs(t.getObject()))
									// Failed on intra-triple pattern join, stop processing of this triple pattern.
									continue patternLoop;
							}
							
						}else if (current.getObject().isVariable())
							// Found a new variable, bind it in the environment.
							setEnvByVarNode(env, current.getObject(), t.getObject());
						// Try to iterate through the satisfied sub queries produced from processing of triple patterns at the last "filter".
						try {
ssqLoop:
							for (IndependentPair<List<TriplePattern>,Node[]> entry : previousSSQs){
								/*
								 *  Compare the environments of the previously satisfied sub queries with the new triple pattern and environment,
								 *  performing an inter-triple pattern join.
								 */
								Node[] otherEnv = entry.getSecondObject();
envLoop:
								for (int i = 0; i < otherEnv.length; i++){
									if (env[i] != null){
										if (otherEnv[i] == null)
											/*
											 * When the new triple pattern environment contains bindings not in the existing environment, copy the binding to
											 * the existing environment.
											 */
											otherEnv[i] = env[i];
										else
											/*
											 * When the new triple pattern environment contains bindings in the existing environment, check that both sets of
											 * bindings are the same.  If not, break out of evaluating the current satisfied sub query, as it cannot join. 
											 */
											if (!env[i].sameValueAs(otherEnv[i]))
												continue ssqLoop;
									}
								}
								/*
								 * If the triple pattern and bindings match with the previously satisfied sub query, add the triple pattern to the
								 * sub query and add it to the list of sub queries satisfied by this "filter" triple.
								 */
								entry.getFirstObject().add(current);
								satisfiedSubQueries.add(entry);
							}
						} catch (NullPointerException e) {
							/*
							 * If there were no satisfied sub queries then there was no previous filter (as when there are no satisfied sub
							 * queries part way through the evaluation of a query, then that query is discarded and the next one started, all
							 * during the next step of processing, as annotated).
							 * As such, add all triple patterns and environments that pass this "filter" to the list of satisfied sub queries.
							 */
							List<TriplePattern> graph = new ArrayList<TriplePattern>();
							graph.add(current);
							satisfiedSubQueries.add(new IndependentPair<List<TriplePattern>,Node[]>(graph,env));
						}
					}
				if (satisfiedSubQueries.isEmpty()){
					/*
					 * partial graph does not match this query, so stop evaluating it.
					 */
					break graphLoop;
				}
				previousSSQs = satisfiedSubQueries;
			}
			/*
			 * Any subQueries in the satisfied  list are subQueries satisfied by the whole of the graph to be routed.
			 */
			for (int i = 0; i < satisfiedSubQueries.size(); i++){
				IndependentPair<List<TriplePattern>,Node[]> entry = satisfiedSubQueries.get(i);
				boolean complete = true;
				for (TriplePattern tp : pattern)
					if (!entry.getFirstObject().contains(tp)){
						complete = false;
						Node[] env = entry.getSecondObject();
						Node subject = tp.getSubject().isVariable()
										? env[((Node_RuleVariable) tp.getSubject()).getIndex()]
										: tp.getSubject(),
							 predicate = tp.getPredicate().isVariable()
							 			? env[((Node_RuleVariable) tp.getPredicate()).getIndex()]
							 			: tp.getPredicate(),
							 object = tp.getObject().isVariable()
							 			? env[((Node_RuleVariable) tp.getObject()).getIndex()]
							 			: tp.getObject();
						TripleMatch boundTM = new Triple(subject,predicate,object);
						
						// add the current query satisfaction index to the list of indexes that find the current bound TripleMatch's query useful.
						List<Integer> ssqList = possibleProbeRefs.get(boundTM);
						try {
							ssqList.add(i);
						} catch (NullPointerException e) {
							ssqList = new ArrayList<Integer>();
							ssqList.add(i);
							possibleProbeRefs.put(boundTM,ssqList);
						}
						
						/*
						 * TODO This last step in the loop is dependent on SteM handling
						 */
						// Incrementally discover the most selective SteM that could answer the bound TripleMatch's query.
						TripleMatch newSteM = tp.asTripleMatch();
						TripleMatch prevSteM = possibleProbeSteMs.get(boundTM);
						try {
							// If the previous SteM subsumes the new SteM, use the new, more selective SteM.
							if ((prevSteM.getMatchSubject() == null || prevSteM.getMatchSubject().sameValueAs(newSteM.getMatchSubject()))
									&& (prevSteM.getMatchPredicate() == null || prevSteM.getMatchPredicate().sameValueAs(newSteM.getMatchPredicate()))
									&& (prevSteM.getMatchObject() == null || prevSteM.getMatchObject().sameValueAs(newSteM.getMatchObject()))){
								possibleProbeSteMs.put(boundTM,newSteM);
							}
						} catch (NullPointerException e) {
							// If no SteM has been chosen for the bound TripleMatch thus far, use the new SteM.
							possibleProbeSteMs.put(boundTM,newSteM);
						}
					}
				if (complete) reportCompletePattern(patternNumber,entry.getSecondObject());
			}
			/*
			 * In Multiquery policies, clear the "network" of the results of the last query.
			 */
			previousSSQs = null;
		}
		
		while (!possibleProbeRefs.keySet().isEmpty()){
			/* 
			 * Use a metric related to observed selectivity, observed window size and reference counting.
			 * Remove queries fulfilled by such routing from the reference counting map.
			 * Send probe to appropriate SteM for selected triple match.
			 */
		}
		
		
		// OLD CODE
		
//		PriorityQueue<TriplePattern> stemQueue = new PriorityQueue<TriplePattern>(this.pattern.size(), new Comparator<TriplePattern>(){
//			@Override
//			public int compare(TriplePattern arg0, TriplePattern arg1) {
//				return SingleQueryPolicyStormGraphRouter.this.stemStats.get(arg0.asTripleMatch())
//						- SingleQueryPolicyStormGraphRouter.this.stemStats.get(arg1.asTripleMatch());
//			}
//		});
//		
//		List<Node[]> envs = new ArrayList<Node[]>();
//		List<Node[]> newEnvs;
//		envs.add(new Node[varCount]);
//		
//		// TODO take into account strict triple patterns that could occlude relaxed triple patterns.  
//		// Iterate over all triple patterns in the graph pattern
//		for (TriplePattern current : this.pattern){
//			newEnvs = new ArrayList<Node[]>();
//			for (Node[] env : envs){
//				//Iterate over all triples in the graph that match the SteM of the current triple pattern.
//				ExtendedIterator<Triple> matchingTriples = g.find(current.asTripleMatch());
//				boolean accountedFor = false;
//				if (accountedFor = matchingTriples.hasNext()){
//					//Initialise subject, object and predicate according to the current environment.
//					Node subject = current.getSubject().isVariable() ? env[((Node_RuleVariable) current.getSubject()).getIndex()] : current.getSubject(),
//						 predicate = current.getPredicate().isVariable() ? env[((Node_RuleVariable) current.getPredicate()).getIndex()] : current.getPredicate(),
//						 object = current.getObject().isVariable() ? env[((Node_RuleVariable) current.getObject()).getIndex()] : current.getObject();
//					//Initialise a variable describing SteM uses unaccounted for with regards to this triple pattern
//					int count = stemRefs.get(current.asTripleMatch());
//					//For each triple that fits the stem, see if it matches the triple pattern (including any previously fixed bindings in the current environment),
//					//then subtract one from the number of unaccounted for uses.
//					while (matchingTriples.hasNext()){
//						Triple match = matchingTriples.next();
//						//If the current triple matches the triple pattern within the binding environment, then create a new environment with the relevant, previously
//						//empty bindings bound with the new values in the triple.
//						if ((subject == null || match.getSubject().equals(subject))
//								&& (predicate == null || match.getPredicate().equals(predicate))
//								&& (object == null || match.getObject().equals(object))){
//							Node[] newEnv = Arrays.copyOf(env, varCount);
//							if (subject == null){
//								newEnv[((Node_RuleVariable) current.getSubject()).getIndex()] = match.getSubject();
//							}
//							if (predicate == null){
//								newEnv[((Node_RuleVariable) current.getPredicate()).getIndex()] = match.getPredicate();
//							}
//							if (object == null){
//								newEnv[((Node_RuleVariable) current.getObject()).getIndex()] = match.getObject();
//							}
//							//Add the new environment to the list of new environments
//							newEnvs.add(newEnv);
//						}
//						//Whether the triple matched within the environment or not, decrement the count of unaccounted for SteM uses.
//						count--;
//					}
//					//If there are unaccounted for uses of the SteM, make a note of it.
//					accountedFor = !(count > 0);
//				}
//				//If there are unaccounted for uses of the SteM, create a new environment that does not fill this triple pattern,
//				//and add this triple pattern to the set of viable triple patterns to route to.
//				if (!accountedFor){
//					newEnvs.add(env);
//					// TODO sort out SteM selection: need to store relevant environment, not just SteM.
//					// TODO sort out SteM selection: need to fully qualify a pattern environment before deciding which SteMs are viable.
//					stemQueue.add(current);
//				}
//			}
//			//Make the new set of environments (those that have led to dead ends removed, new branches from the most recent triple pattern added)
//			//the base set of environments.
//			envs = newEnvs;
//		}
//		
//		// By this stage the probing graph has been verified against the current pattern for all environments.
//		// Check to see if the probing graph matches the current pattern completely
//		// (only requires checking that the graph and the pattern are the same size)
//		if (g.size() == pattern.size()) {
//			for (Node[] env : envs)
//				this.reportCompletePattern(env);
//			return;
//		}
//		
//		//TODO select a stem to send to, then send a probe request to it, satisfying as many environments as possible.
	}
	
	private void reportCompletePattern(int patternNumber, Node[] env) {
		System.out.println(String.format("Pattern %s complete, with bindings:", (Object) patterns[patternNumber]));
		System.out.println(env.toString());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		Set<TripleMatch> stems = new HashSet<TripleMatch>();
		try{
			for (String query : queries){
				// Convert query string to graph pattern
				TriplePattern[] pattern = (TriplePattern[]) Rule.parseRule(query).getBody();
				// Iterate through all triple patterns in the graph pattern
				for (TriplePattern tp : pattern){
					// If the stem that provides for the current query pattern hasn't been seen before
					TripleMatch tm = tp.asTripleMatch();
					if (stems.add(tm)){
						declarer.declareStream(String.format("%s,%s,%s",
																tm.getMatchSubject() == null ? "" : tm.getMatchSubject().toString(),
																tm.getMatchPredicate() == null ? "" : tm.getMatchPredicate().toString(),
																tm.getMatchObject() == null ? "" : tm.getMatchObject().toString()
															),
													new Fields("s","p","o",
																Component.action.toString(),
																Component.isAdd.toString(),
																Component.graph.toString(),
																Component.timestamp.toString()
															)
												);
					}
				}
			}
		} catch (ClassCastException e){}
	}
	
	// INNER CLASSES
	
	public static class MQPEddyStubStormGraphRouter extends EddyStubStormGraphRouter {

		private static final long serialVersionUID = -1974101140071769900L;

		public MQPEddyStubStormGraphRouter(List<String> eddies) {
			super(eddies);
		}
		
		protected void prepare(){
			
		}

		@Override
		protected void distributeToEddies(Tuple anchor, Values vals) {
			String source = anchor.getSourceComponent();
			if (this.eddies.contains(source)){
				logger.debug(String.format("\nRouting back to Eddy %s for %s", source, vals.get(0).toString()));
				this.collector.emit(source, anchor, vals);
			}else
				for (String eddy : this.eddies){
					logger.debug(String.format("\nRouting on to Eddy %s for %s from %s", eddy, vals.get(0).toString(), source));
					this.collector.emit(eddy, anchor, vals);
				}
			this.collector.ack(anchor);
		}
		
	}

}
