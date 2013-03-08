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
public class SingleQueryPolicyStormGraphRouter extends StormGraphRouter {

	private static final long serialVersionUID = 4342744138230718341L;
	protected final static Logger logger = Logger.getLogger(SingleQueryPolicyStormGraphRouter.class);
	protected static int[] FACTORIALS = {0,1,2,6,24,120,720};
	

	private String query;
	
	/**
	 * @param q
	 */
	public SingleQueryPolicyStormGraphRouter(String q){
		this.query = q;
	}
	
	private int varCount;
	private List<TriplePattern> pattern;
	private Map<TripleMatch,Integer> stemStats;
	private Map<TripleMatch,Integer> stemRefs;
	
	protected void prepare(){
		Rule rule = Rule.parseRule(query);
		varCount = rule.getNumVars();
		stemRefs = new HashMap<TripleMatch,Integer>();
		try{
			int count = 0;
			pattern = Arrays.asList((TriplePattern[]) rule.getBody());
			for (TriplePattern tp : pattern){
				stemStats.put(tp.asTripleMatch(), count++);
				if (stemRefs.containsKey(tp.asTripleMatch()))
					stemRefs.put(tp.asTripleMatch(), stemRefs.get(tp.asTripleMatch()) + 1);
				else
					stemRefs.put(tp.asTripleMatch(), 1);
			}
		} catch (ClassCastException e){}
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
		Map<TripleMatch,List<Integer>> possibleProbes = new HashMap<TripleMatch,List<Integer>>();
		
		List<IndependentPair<List<TriplePattern>,Node[]>> previousSSQs = null;
		List<IndependentPair<List<TriplePattern>,Node[]>> satisfiedSubQueries = null;
		
		ExtendedIterator<Triple> triples = g.find(null,null,null);
graphLoop:
		for (Triple t = triples.next(); triples.hasNext(); t = triples.next()){
			satisfiedSubQueries = new ArrayList<IndependentPair<List<TriplePattern>,Node[]>>();
patternLoop:
			for (TriplePattern current : pattern)
				
				if ((current.getSubject().isVariable() || current.getSubject().sameValueAs(t.getSubject()))
						&& (current.getPredicate().isVariable() || current.getPredicate().sameValueAs(t.getPredicate()))
						&& (current.getObject().isVariable() || current.getObject().sameValueAs(t.getObject()))){
					Node[] env = new Node[varCount];
					
					if (current.getSubject().isVariable()){
						setEnvByVarNode(env, current.getSubject(), t.getSubject());
						
						if (current.getPredicate().isVariable()){
							if (((Node_RuleVariable) current.getSubject()).getIndex() != ((Node_RuleVariable) current.getPredicate()).getIndex())
								setEnvByVarNode(env, current.getPredicate(), t.getPredicate());
							else if (!t.getSubject().sameValueAs(t.getPredicate()))
								continue patternLoop;
							
							if (current.getObject().isVariable()){
								if (((Node_RuleVariable) current.getPredicate()).getIndex() != ((Node_RuleVariable) current.getObject()).getIndex())
									setEnvByVarNode(env, current.getObject(), t.getObject());
								else if (!t.getPredicate().sameValueAs(t.getObject()))
									continue patternLoop;
							}
							
						}else if (current.getObject().isVariable()){
							if (((Node_RuleVariable) current.getSubject()).getIndex() != ((Node_RuleVariable) current.getObject()).getIndex())
								setEnvByVarNode(env, current.getObject(), t.getObject());
							else if (!t.getSubject().sameValueAs(t.getObject()))
								continue patternLoop;
						}
						
					}else if (current.getPredicate().isVariable()){
						setEnvByVarNode(env, current.getPredicate(), t.getPredicate());
						
						if (current.getObject().isVariable()){
							if (((Node_RuleVariable) current.getPredicate()).getIndex() != ((Node_RuleVariable) current.getObject()).getIndex())
								setEnvByVarNode(env, current.getObject(), t.getObject());
							else if (!t.getPredicate().sameValueAs(t.getObject()))
								continue patternLoop;
						}
						
					}else if (current.getObject().isVariable())
						setEnvByVarNode(env, current.getObject(), t.getObject());
					
					try {
						for (IndependentPair<List<TriplePattern>,Node[]> entry : previousSSQs){
							Node[] otherEnv = entry.getSecondObject();
							boolean passed = true;
							for (int i = 0; i < otherEnv.length; i++){
								if (env[i] != null){
									if (otherEnv[i] == null)
										otherEnv[i] = env[i];
									else
										passed &= env[i].sameValueAs(otherEnv[i]);
								}
							}
							if (passed){
								entry.getFirstObject().add(current);
								satisfiedSubQueries.add(entry);
							}
						}
					} catch (NullPointerException e) {
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
		 * Any subQueries in the nextSSQs list are subQueries satisfied by the whole of the graph to be routed.
		 */
		for (int i = 0; i < satisfiedSubQueries.size(); i++){
			IndependentPair<List<TriplePattern>,Node[]> entry = satisfiedSubQueries.get(i);
			for (TriplePattern tp : pattern)
				if (!entry.getFirstObject().contains(tp)){
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
					List<Integer> ssqList = possibleProbes.get(boundTM);
					try {
						ssqList.add(i);
					} catch (NullPointerException e) {
						ssqList = new ArrayList<Integer>();
						ssqList.add(i);
						possibleProbes.put(boundTM,ssqList);
					}
				}
		}
		/*
		 * In Multiquery policies, clear the "network" of the results of the last query.
		 */
//		previousSSQs = null;
		
		while (!possibleProbes.keySet().isEmpty()){
			/* 
			 * Use a metric related to observed selectivity, observed window size and reference counting.
			 * Remove queries fulfilled by such routing from the reference counting map.
			 * Send probe to appropriate SteM for selected triple match.
			 */
		}
		
		/*
		 * Use the Rete algorithm in reverse:
		 * 		Form an unoptimised Rete-like network from the Graph g.  Joins are arbitrary in terms of network structure.
		 * 		Pass the triple patterns into the network, each with an associated binding environment as long as the highest
		 * 			index it is aware of.  Filtering happens in reverse, so every triple pattern matched by a given triple in
		 * 			g enters the network at that location (as well as any others matched by it).  Variables are bound in the
		 * 			environments at this stage.
		 * 		Meaningful joins occur when two subgraph patterns from the same query share a variable(s).  The values stored
		 * 			in their environments are compared.  If they match then the largest any other bindings from the smaller
		 * 			environment are added to the larger, and the combined graph pattern is passed on to the next join node.
		 * 			If there is no match then the patterns are retained at that node in case there is an alternate completion
		 * 			available.
		 *		Other joins simply combine two unrelated subgraph patterns and pass the result to the next node.  Incongruities
		 *			will be caught by meaningful joins higher up the tree.
		 *		If unchecked queries remain, clear the network (except the output patterns and binding environments) and repeat
		 *			with the next query.
		 * If there are no query instantiations that match at the final join, stop processing, emit no new probes.
		 * Otherwise, find the set of triple patterns left unmatched:
		 * 		Remove all triple patterns in the first successful joining from the set of triple patterns in the respective query.
		 * End ---> If all triple patterns are accounted for by the joining, report a successful query completion and start
		 * 			loop again with next successful joining.
		 * 		Populate the remaining triple patterns from the successful binding environment.
		 * 		Add populated triple patterns to a reference counting map.
		 * 		Repeat for all successful joinings of all queries.
		 * If there are no triple patterns in the reference counting map, stop processing, emit no new probes.
		 * Otherwise, find the optimal SteM(s) to route to, and with what data.
		 * 		Use a metric related to observed selectivity, observed window size and reference counting.
		 * 		Remove queries fulfilled by such routing from the reference counting map.
		 * 		Send probe to selected SteM.
		 * 		Repeat for as long as there are triple patterns in the reference counting map.
		 */
		
		
		
		
		PriorityQueue<TriplePattern> stemQueue = new PriorityQueue<TriplePattern>(this.pattern.size(), new Comparator<TriplePattern>(){
			@Override
			public int compare(TriplePattern arg0, TriplePattern arg1) {
				return SingleQueryPolicyStormGraphRouter.this.stemStats.get(arg0.asTripleMatch())
						- SingleQueryPolicyStormGraphRouter.this.stemStats.get(arg1.asTripleMatch());
			}
		});
		
		List<Node[]> envs = new ArrayList<Node[]>();
		List<Node[]> newEnvs;
		envs.add(new Node[varCount]);
		
		// TODO take into account strict triple patterns that could occlude relaxed triple patterns.  
		// Iterate over all triple patterns in the graph pattern
		for (TriplePattern current : this.pattern){
			newEnvs = new ArrayList<Node[]>();
			for (Node[] env : envs){
				//Iterate over all triples in the graph that match the SteM of the current triple pattern.
				ExtendedIterator<Triple> matchingTriples = g.find(current.asTripleMatch());
				boolean accountedFor = false;
				if (accountedFor = matchingTriples.hasNext()){
					//Initialise subject, object and predicate according to the current environment.
					Node subject = current.getSubject().isVariable() ? env[((Node_RuleVariable) current.getSubject()).getIndex()] : current.getSubject(),
						 predicate = current.getPredicate().isVariable() ? env[((Node_RuleVariable) current.getPredicate()).getIndex()] : current.getPredicate(),
						 object = current.getObject().isVariable() ? env[((Node_RuleVariable) current.getObject()).getIndex()] : current.getObject();
					//Initialise a variable describing SteM uses unaccounted for with regards to this triple pattern
					int count = stemRefs.get(current.asTripleMatch());
					//For each triple that fits the stem, see if it matches the triple pattern (including any previously fixed bindings in the current environment),
					//then subtract one from the number of unaccounted for uses.
					while (matchingTriples.hasNext()){
						Triple match = matchingTriples.next();
						//If the current triple matches the triple pattern within the binding environment, then create a new environment with the relevant, previously
						//empty bindings bound with the new values in the triple.
						if ((subject == null || match.getSubject().equals(subject))
								&& (predicate == null || match.getPredicate().equals(predicate))
								&& (object == null || match.getObject().equals(object))){
							Node[] newEnv = Arrays.copyOf(env, varCount);
							if (subject == null){
								newEnv[((Node_RuleVariable) current.getSubject()).getIndex()] = match.getSubject();
							}
							if (predicate == null){
								newEnv[((Node_RuleVariable) current.getPredicate()).getIndex()] = match.getPredicate();
							}
							if (object == null){
								newEnv[((Node_RuleVariable) current.getObject()).getIndex()] = match.getObject();
							}
							//Add the new environment to the list of new environments
							newEnvs.add(newEnv);
						}
						//Whether the triple matched within the environment or not, decrement the count of unaccounted for SteM uses.
						count--;
					}
					//If there are unaccounted for uses of the SteM, make a note of it.
					accountedFor = !(count > 0);
				}
				//If there are unaccounted for uses of the SteM, create a new environment that does not fill this triple pattern,
				//and add this triple pattern to the set of viable triple patterns to route to.
				if (!accountedFor){
					newEnvs.add(env);
					// TODO sort out SteM selection: need to store relevant environment, not just SteM.
					// TODO sort out SteM selection: need to fully qualify a pattern environment before deciding which SteMs are viable.
					stemQueue.add(current);
				}
			}
			//Make the new set of environments (those that have led to dead ends removed, new branches from the most recent triple pattern added)
			//the base set of environments.
			envs = newEnvs;
		}
		
		// By this stage the probing graph has been verified against the current pattern for all environments.
		// Check to see if the probing graph matches the current pattern completely
		// (only requires checking that the graph and the pattern are the same size)
		if (g.size() == pattern.size()) {
			this.reportCompletePattern(envs);
			return;
		}
		
		//TODO select a stem to send to, then send a probe request to it, satisfying as many environments as possible.
	}
	
	private void reportCompletePattern(List<Node[]> bindings) {
		System.out.println(String.format("Pattern %s complete, with bindings:", (Object) pattern));
		for (Node[] binding : bindings)
			System.out.println(binding.toString());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		Set<TripleMatch> stems = new HashSet<TripleMatch>();
		try{
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
		} catch (ClassCastException e){}
	}
	
	// INNER CLASSES
	
	public static class SQPEddyStubStormGraphRouter extends EddyStubStormGraphRouter {

		private static final long serialVersionUID = -1974101140071769900L;

		public SQPEddyStubStormGraphRouter(List<String> eddies) {
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
