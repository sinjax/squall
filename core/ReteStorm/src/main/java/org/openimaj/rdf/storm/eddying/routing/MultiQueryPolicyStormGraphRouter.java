package org.openimaj.rdf.storm.eddying.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter.EddyingStormGraphRouter;
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
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A StormGraphRouter implementation that routes graphs according to a policy incorporating potentially many distinct and/or overlapping queries/rules.
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class MultiQueryPolicyStormGraphRouter extends EddyingStormGraphRouter {

	private static final long serialVersionUID = 4342744138230718341L;
	protected final static Logger logger = Logger.getLogger(MultiQueryPolicyStormGraphRouter.class);
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static enum TriplePart {
		/**
		 * 
		 */
		subject
		,
		/**
		 * 
		 */
		predicate
		,
		/**
		 * 
		 */
		object
//		,
//		/**
//		 * 
//		 */
//		timestamp
		;
		
		private static String[] strings;
		static {
			Component[] vals = Component.values();
			strings = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strings[i] = vals[i].toString();
			}
		}

		/**
		 * @return like {@link #values()} but {@link String} instances
		 */
		public static String[] strings() {
			return strings;
		}
	}
	
	protected final Fields fields;
	
	/**
	 * Creates an new instance of a StormGraphRouter with the multi-query policy.
	 * @param s - Map of {@link TripleMatch}es in string form to SteM names   
	 * @param q - A string containing a set of Jena {@link Rule}s
	 */
	public MultiQueryPolicyStormGraphRouter(Map<String,String> s, String q){
		super(s,q);
		fields = new Fields(TriplePart.subject.toString(),
							   TriplePart.predicate.toString(),
							   TriplePart.object.toString(),
							   Component.action.toString(),
							   Component.isAdd.toString(),
							   Component.graph.toString(),
							   Component.timestamp.toString(),
							   Component.duration.toString()
							);
	}
	
	private int[] varCount;
	private List<List<TriplePattern>> patterns;
	private Map<TripleMatch,Integer> stemStats;
	private Map<TripleMatch,Integer> stemRefs;
	
	protected void prepare(){
		stemStats = new HashMap<TripleMatch,Integer>();
		stemRefs = new HashMap<TripleMatch,Integer>();
		List<Rule> rules = Rule.parseRules(queries);
		varCount = new int[rules.size()];
		patterns = new ArrayList<List<TriplePattern>>();
		for (int i = 0; i < rules.size(); i++){
			Rule rule = rules.get(i);
			varCount[i] = rule.getNumVars();
			try{
				int count = 0;
				List<TriplePattern> l = new ArrayList<TriplePattern>();
				for (ClauseEntry ce : rule.getBody()){
					TriplePattern tp = (TriplePattern) ce;
					l.add(tp);
					stemStats.put(tp.asTripleMatch(), count++);
					if (stemRefs.containsKey(tp.asTripleMatch()))
						stemRefs.put(tp.asTripleMatch(), stemRefs.get(tp.asTripleMatch()) + 1);
					else
						stemRefs.put(tp.asTripleMatch(), 1);
				}
				patterns.add(l);
			} catch (ClassCastException e){
				e.printStackTrace();
				System.exit(1);
			} catch (NullPointerException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		logger.debug(String.format("\nInitial Patterns: %s",
				   patterns.toString()));
	}
	
	@Override
	public void cleanup(){
		varCount = null;
		patterns = null;
		stemStats = null;
		stemRefs = null;
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
		this.collector.ack(anchor);
		
		Map<TripleMatch,List<Integer>> possibleProbeRefs = new HashMap<TripleMatch,List<Integer>>();
		Map<TripleMatch,TripleMatch> possibleProbeSteMs = new HashMap<TripleMatch,TripleMatch>();
		
queryLoop:
		for (int patternNumber = 0; patternNumber < patterns.size(); patternNumber++){
			List<TriplePattern> pattern = patterns.get(patternNumber);
			
			List<IndependentPair<List<TriplePattern>,Node[]>> previousSSQs = null;
			List<IndependentPair<List<TriplePattern>,Node[]>> satisfiedSubQueries = null;
			
			logger.debug(String.format("\nChecking received graph against pattern:\n%s\n%s",
					   g.toString(),
					   pattern.toString()));
			// For each triple in the partial graph (the "filter nodes" of the inverse Rete network)...
graphLoop:
			for (ExtendedIterator<Triple> triples = g.find(null,null,null); triples.hasNext();){
				Triple t = triples.next();
				// ... create a list of sub-queries to be populated with the sub-queries satisfied by the new triple combined with those previously tested,
				// then loop through all triple patterns in the current query...
				satisfiedSubQueries = new ArrayList<IndependentPair<List<TriplePattern>,Node[]>>();
patternLoop:
				for (TriplePattern current : pattern){
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
				}
				if (satisfiedSubQueries.isEmpty()){
					/*
					 * partial graph does not match this query, so stop evaluating it.
					 */
					continue queryLoop;
				}
				previousSSQs = satisfiedSubQueries;
			}
			logger.debug(String.format("\nSatisfied Sub Queries: %s",
					   satisfiedSubQueries.toString()));
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
										? env[((Node_RuleVariable) tp.getSubject()).getIndex()] == null
								 			? Node.ANY
										 	: env[((Node_RuleVariable) tp.getSubject()).getIndex()]
										: tp.getSubject(),
							 predicate = tp.getPredicate().isVariable()
							 			? env[((Node_RuleVariable) tp.getPredicate()).getIndex()] == null
								 			? Node.ANY
										 	: env[((Node_RuleVariable) tp.getPredicate()).getIndex()]
							 			: tp.getPredicate(),
							 object = tp.getObject().isVariable()
							 			? env[((Node_RuleVariable) tp.getObject()).getIndex()] == null
							 				? Node.ANY
							 				: env[((Node_RuleVariable) tp.getObject()).getIndex()]
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
				if (complete) reportCompletePattern(patternNumber,g,timestamp);
			}
		}
		
		while (!possibleProbeRefs.keySet().isEmpty()){
			/*
			 * Use a metric related to observed selectivity, observed window size and reference counting to select a SteM and query pair.
			 * TODO research a more sophisticated metric.
			 */
			TripleMatch selected = null;
			for (TripleMatch potential : possibleProbeRefs.keySet()) try {
				if (possibleProbeRefs.get(selected).size() < possibleProbeRefs.get(potential).size())
					selected = potential;
			} catch (NullPointerException e) {
				selected = potential;
			}
			/*
			 * Remove queries fulfilled by such routing from the reference counting map.
			 */
			Map<TripleMatch, List<Integer>> remainingProbeRefs = new HashMap<TripleMatch,List<Integer>>();
TMLoop:
			for (TripleMatch other : possibleProbeRefs.keySet()) {
SSQLoop:
				for (int ssq : possibleProbeRefs.get(selected)){
					if (possibleProbeRefs.get(other).contains(ssq))
						continue TMLoop;
				}
				remainingProbeRefs.put(other, possibleProbeRefs.get(other));
			}
			possibleProbeRefs = remainingProbeRefs;
			/*
			 * Send probe to appropriate SteM for selected triple match.
			 * TODO provide some sort of triple match independent way to reference SteMs, probably by some sort of dictionary.
			 */
			TripleMatch stem = possibleProbeSteMs.get(selected);
			String stemName = stemMap.get(String.format("%s %s %s .",
															stem.getMatchSubject() == null ? "?" : stem.getMatchSubject().toString(),
															stem.getMatchPredicate() == null ? "?" : stem.getMatchPredicate().toString(),
															stem.getMatchObject() == null ? "?" : stem.getMatchObject().toString()
														));
			
			Values vals = new Values();
			vals.add(selected.getMatchSubject() == null ? Node.createVariable("s") : selected.getMatchSubject());
			vals.add(selected.getMatchPredicate() == null ? Node.createVariable("p") : selected.getMatchPredicate());
			vals.add(selected.getMatchObject() == null ? Node.createVariable("o") : selected.getMatchObject());
			vals.add(Action.probe);
			vals.add(isAdd);
			vals.add(g);
			vals.add(timestamp);
			vals.add((long)600000);
			logger.debug(String.format("\nRouting triple: %s %s %s\nto SteM: %s",
									   vals.get(0),
									   vals.get(1),
									   vals.get(2),
									   stemName));
			this.collector.emit(stemName, anchor, vals);
		}
	}
	
	int complete = 0;
	
	private void reportCompletePattern(int patternNumber, Graph g, long timestamp) {
		complete++;
		System.out.println(String.format("Pattern %s complete, with bindings:\n%s triggered at %d\n%d complete patterns",
											(Object) patterns.get(patternNumber),
											g.toString(),
											timestamp,
											complete));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		Set<TripleMatch> stems = new HashSet<TripleMatch>();
		try{
			for (Rule rule : Rule.parseRules(queries)){
				// Convert query string to graph pattern
				ClauseEntry[] pattern = rule.getBody();
				// Iterate through all triple patterns in the graph pattern
				for (ClauseEntry ce : pattern){
					TriplePattern tp = (TriplePattern) ce;
					// If the stem that provides for the current query pattern hasn't been seen before
					TripleMatch tm = tp.asTripleMatch();
					if (stems.add(tm)){
						declarer.declareStream(stemMap.get(String.format("%s %s %s .",
																			tm.getMatchSubject() == null ? "?" : tm.getMatchSubject().toString(),
																			tm.getMatchPredicate() == null ? "?" : tm.getMatchPredicate().toString(),
																			tm.getMatchObject() == null ? "?" : tm.getMatchObject().toString()
																		)),
													fields
												);
					}
				}
			}
		} catch (ClassCastException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public Fields getFields() {
		return fields;
	}
	
	// INNER CLASSES
	
	/**
	 * The stub StormGraphRouter that simply routes graphs back to a MultiQueryPolicyStormGraphRouter for intelligent routing.
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 */
	public static class MQPEddyStubStormGraphRouter extends EddyStubStormGraphRouter {

		private static final long serialVersionUID = -1974101140071769900L;

		/**
		 * Creates a new stub router for routing data back to Eddies acting on a MultiQuery policy.
		 * @param eddies - The list of eddies that this stub router can route to (usually only one, but could be many)
		 */
		public MQPEddyStubStormGraphRouter(List<String> eddies) {
			super(eddies);
		}
		
		@Override
		protected void prepare(){
			// No preparation needed.
		}
		
		@Override
		public void cleanup(){
			// No preparation to undo.
		}

		@Override
		protected void distributeToEddies(Tuple anchor, Values vals) {
			String source;
			try {
				source = anchor.getSourceComponent();
				if (this.eddies.contains(source)){
					logger.debug(String.format("\nRouting back to Eddy %s for %s", source, vals.get(0).toString()));
					this.collector.emit(source, anchor, vals);
					return;
				}
			} catch (NullPointerException e) {
				source = "no source";
			}
			for (String eddy : this.eddies){
				logger.debug(String.format("\nRouting on to Eddy %s for %s from %s", eddy, vals.get(0).toString(), source));
				this.collector.emit(eddy, anchor, vals);
			}
		}
		
	}

}
