/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.eddying.stems;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter.Action;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.rdf.storm.topology.logging.LoggerBolt.*;
import org.openimaj.rdf.storm.utils.CircularPriorityWindow;

import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.compose.Polyadic;

/**
 * Represents one input left of a join node. The queue points to
 * a sibling queue representing the other leg which should be joined
 * against.
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>, based largely on the RETEQueue
 *         implementation by <a href="mailto:der@hplb.hpl.hp.com">Dave
 *         Reynolds</a>
 */
public class StormSteMQueue implements CircularPriorityWindow.DurationOverflowHandler<StormSteMQueue.Environment>, CircularPriorityWindow.CapacityOverflowHandler<StormSteMQueue.Environment> {

	protected final static Logger logger = Logger.getLogger(StormSteMQueue.class);
	private static final boolean logging = false;
	private LogEmitter logStream;

	/** A time-prioritised and size limited sliding window of Tuples */
	private final CircularPriorityWindow<Environment> window;

	/** A count of {@link Fields} which should match between the two inputs */
	private int varCount;

	/** The router that results should be passed on to */
	protected StormGraphRouter router;

	/**
	 * Constructor. The window is not usable until it has been bound
	 * to a StormGraphRouter.
	 * @param vars 
	 * @param size
	 * @param delay
	 * @param unit
	 * @param le 
	 */
	public StormSteMQueue(int vars,
						  int size,
						  long delay,
						  TimeUnit unit,
						  LogEmitter le) {
		this.varCount = vars;
		this.window = new CircularPriorityWindow<Environment>(this, size, delay, unit);
		if (logging)
			this.logStream = le;
	}
	
	/**
	 * 
	 * @param sink
	 */
	public void setGraphRouter(StormGraphRouter sink) {
		this.router = sink;
	}
	
	/**
	 * Constructor. The window is now usable.
	 * @param vars 
	 * @param size
	 * @param delay
	 * @param unit
	 * @param le 
	 * @param sgr 
	 */
	public StormSteMQueue(int vars,
						  int size,
						  long delay,
						  TimeUnit unit,
						  LogEmitter le,
						  StormGraphRouter sgr) {
		this(vars,size,delay,unit,le);
		this.setGraphRouter(sgr);
	}

	/**
	 * Build a tuple into this SteM.
	 * 
	 * @param env
	 *            a set of variable bindings for the rule being processed.
	 * @param isAdd
	 *            distinguishes between add and remove operations.
	 * @param timestamp
	 *            the time at which the triple was added from the stream
	 */
	public void build(boolean isAdd, List<Node> vars, Graph graph, long timestamp, long duration) {
		Environment newEnv = new Environment(vars, graph, timestamp, duration);
		if (isAdd)
			// Store the new token in this store
			this.window.offer(newEnv);
		else
			// Remove any existing instances of the token from this store
			this.window.remove(newEnv);
	}
	
	private interface JoinDetector {
		
		public boolean compare(Node n1, Node n2);
		
	}
	
	private static class NotJoined implements JoinDetector {

		@Override
		public boolean compare(Node n1, Node n2) {
			return ( !( n1.sameValueAs(n2) || n1.isVariable() ) );
		}
		
	}
	
	private static class Joined implements JoinDetector {

		@Override
		public boolean compare(Node n1, Node n2) {
			return n1.sameValueAs(n2);
		}
		
	}
	
	protected static Graph joinSubGraphs(Tuple thisTuple, Environment steMEnv) {
		Polyadic newG = new MultiUnion();
		newG.addGraph((Graph) thisTuple.getValueByField(Component.graph.toString()));
		newG.addGraph(steMEnv.getGraph());
		return newG;
	}
	
	private void probeImpl(Tuple env, boolean isAdd, long timestamp, long duration, JoinDetector failureCondition){
		// Cross match new token against the entries in the sibling queue
		List<Object> values = env.getValues();
		logger.debug("\nChecking new tuple values: " + StormReteBolt.cleanString(values));
		logger.debug("\nComparing new tuple to " + this.window.size() + " other tuples");
		boolean matched = false;
		for (Iterator<Environment> i = this.window.iterator(); i.hasNext();) {
			Environment candidate = i.next();
			long candStamp = candidate.getTimestamp();
			// If oldest remaining tuple is newer than the probing tuple, don't bother probing against the rest of the window.
			if (timestamp < candStamp)
				break;
			// If oldest remaining tuple is not new enough to match the query being carried out, skip it.
			if (candStamp + duration < timestamp)
				continue;
			boolean matchFailed = false;
			for (int j = 0; j < this.varCount; j++) {
				// If the queue match indices indicate there should be a match get the
				// values of j in the queue and matchIndices[j] in the sibling
				Node thisNode = (Node) values.get(j);
				Node steMNode = (Node) candidate.getVars().get(j);
				// If this pair of nodes meet the failure condition, set "matchFailed" to true, and don't bother checking the rest of the node pairs.
				if (matchFailed = failureCondition.compare(thisNode, steMNode))
					break;
			}
			// if the match succeeded, alert the probe operation that a successful match was found for the input tuple and route the output.
			if (matchFailed == false) {
				
				matched = true;
				
				// Instantiate a new combined graph
				Graph g = joinSubGraphs(env, candidate);
				logger.debug("\nMatch Found! preparing for emit!\n"+g.toString());
				// initiate graph routing
				this.router.routeGraph(env, Action.probe, isAdd, g, timestamp, candStamp);
			}
		}
		// if no match was found, report this fact
		if (matched == false)
			logger.debug(String.format("\nCould not match partially complete graph: %s\nTook %s milliseconds.", env.getValueByField(Component.graph.toString()).toString(), (new Date().getTime() - timestamp)));
	}
	
	/**
	 * Probe a tuple into this SteM.
	 * 
	 * @param env
	 *            a set of variable bindings for the rule being processed.
	 * @param isAdd
	 *            distinguishes between add and remove operations.
	 * @param timestamp
	 *            the time at which the triple was added from the stream
	 * @param duration
	 * 			  the age of the triples to be accepted 
	 */
	public void probe(Tuple env, boolean isAdd, long timestamp, long duration) {
		probeImpl(env, isAdd, timestamp, duration, new NotJoined());
	}
	
	/**
	 * Probe a tuple negatively into this SteM.
	 * 
	 * @param env
	 *            a set of variable bindings for the rule being processed.
	 * @param isAdd
	 *            distinguishes between add and remove operations.
	 * @param timestamp
	 *            the time at which the triple was added from the stream
	 * @param duration
	 * 			  the age of the triples to be accepted 
	 */
	public void negativeProbe(Tuple env, boolean isAdd, long timestamp, long duration) {
		probeImpl(env, isAdd, timestamp, duration, new Joined());
	}

	@Override
	public void handleCapacityOverflow(Environment overflow) {
		logger.debug("Window capacity exceeded.");
		if (logging)
			logStream.emit(new LoggedEvent<Environment>(LoggedEvent.EventType.TUPLE_DROPPED,
													  overflow, "capacity"));
	}

	@Override
	public void handleDurationOverflow(Environment overflow) {
		logger.debug("Tuple exceeded age of window.");
		if (logging)
			logStream.emit(new LoggedEvent<Environment>(LoggedEvent.EventType.TUPLE_DROPPED,
													  overflow, "duration"));
		Values vals = new Values();
		vals.addAll(overflow.getVars());
		vals.add(Action.build);
		vals.add(true);
		vals.add(overflow.getGraph());
		vals.add(overflow.getTimestamp());
		vals.add(overflow.getDuration());
//		this.continuation.fire("old", vals, true);
//		this.continuation.emit("old", overflow);
	}
	
	static class Environment {
		
		private final List<Node> vars;
		private final Graph graph;
		private final long timestamp;
		private final long duration;
		
		public Environment(List<Node> v, Graph g, long t, long d){
			this.vars = v;
			this.graph = g;
			this.timestamp = t;
			this.duration = d;
		}
		
		public List<Node> getVars(){
			return this.vars;
		}
		
		public Graph getGraph(){
			return this.graph;
		}
		
		public long getTimestamp(){
			return this.timestamp;
		}
		
		public long getDuration(){
			return this.duration;
		}
		
		public boolean equals(Object arg0) {
			try {
				Environment other = (Environment)arg0;
				return this.graph.equals(other.graph);
			} catch (ClassCastException e) {
				return false;
			} catch (NullPointerException e) {
				return false;
			}
		}
		
	}

}