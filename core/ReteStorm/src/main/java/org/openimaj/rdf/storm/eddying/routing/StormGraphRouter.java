package org.openimaj.rdf.storm.eddying.routing;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.EddyingBolt;
import org.openimaj.rdf.storm.eddying.stems.StormSteMQueue;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;

import backtype.storm.task.OutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;

/**
 * The abstract class representing the implementation of some policy for routing {@link Triple}s and {@link Graph}s around a Storm topology.
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public abstract class StormGraphRouter implements Serializable {
	
	private static final long serialVersionUID = -3433809982075329507L;
	protected final static Logger logger = Logger.getLogger(StormGraphRouter.class);

	/**
	 * The set of actions that SteMs can carry out on data.
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 */
	public static enum Action {

		/**
		 *
		 */
		check
		,
		/**
		 * 
		 */
		build
		,
		/**
		 * 
		 */
		cancelBuild
		,
		/**
		 *
		 */
		probe
		;
		private static String[] strings;
		static {
			Action[] vals = Action.values();
			strings = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				strings[i] = vals[i].toString();
			}
		}

		/**
		 * 
		 * @return like {@link #values()} but {@link String} instances
		 */
		public static String[] strings() {
			return strings;
		}
	}

	protected EddyingBolt collector;
	
	protected abstract void prepare();
	
	/**
	 * Prepares the StormGraphRouter for the shutdown of the bolt it is acting in.
	 */
	public abstract void cleanup();
	
	/**
	 * 
	 * @param c - The Bolt in which the router is operating, which will service all its calls to emit data
	 */
	public void setOutputCollector(EddyingBolt c){
		this.collector = c;
		this.prepare();
	}
	
	protected abstract long routingTimestamp(long stamp1, long stamp2);
	
	/**
	 * Determine which timestamp to use, and whether the {@link Graph} g should be routed based on the timestamps provided. 
	 * Then route the {@link Graph} g according to the internal policy, using the {@link Tuple} anchor as the Storm anchor, applying the stated action at its destination.
	 * @param anchor 
	 * @param g
	 * @param action 
	 * @param isAdd
	 * @param newtimestamp
	 * @param oldtimestamp
	 */
	public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g, long newtimestamp, long oldtimestamp){
		logger.debug(String.format("\nComparing timestamps: %s and %s",newtimestamp,oldtimestamp));
		long ts = routingTimestamp(newtimestamp, oldtimestamp);
		if (ts >= 0)
			routeGraph(anchor, action, isAdd, g, ts);
	}
	
	/**
	 * Route the {@link Graph} g according to the internal policy, using the {@link Tuple} anchor as the Storm anchor, applying the stated action at its destination.
	 * @param anchor 
	 * @param g
	 * @param isBuild 
	 * @param isAdd
	 * @param timestamp
	 */
	public abstract void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g, long timestamp);
	
	/**
	 * route the {@link Graph} g (which should contain a single {@link Triple}) according to the internal policy, using the {@link Tuple} anchor as the Storm anchor, applying the stated action at its destination.
	 * @param anchor 
	 * @param g
	 * @param action 
	 * @param isAdd
	 * @param timestamp
	 */
	public abstract void routeTriple(Tuple anchor, Action action, boolean isAdd, Graph g, long timestamp);
	
	/**
	 * Declares the Storm output streams, and their fields, required by the router.
	 * @param declarer - the {@link OutputFieldsDeclarer} for the Bolt in which the StormGraphRouter is acting.
	 */
	public abstract void declareOutputFields(OutputFieldsDeclarer declarer);
	
	/**
	 * An abstract class providing functionality common to all routers whose purpose is to simply route data from some SteM to some Eddy.  This common functionality comprises of storing a set of all the Eddies in a system that one could route to, and providing the first step in a routing policy:
	 * "Construct a value set from the provided data without processing the contents of the {@link Graph}, as that is irrelevant, then distribute to the Eddies (to be defined by subclasses)." 
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 */
	public static abstract class EddyStubStormGraphRouter extends StormGraphRouter {
		
		private static final long serialVersionUID = -3683430533570003022L;
		protected final List<String> eddies;
		
		/**
		 * 
		 * @param eddies 
		 * 			The list of eddies this router's SteM is part of.
		 */
		public EddyStubStormGraphRouter(List<String> eddies){
			this.eddies = eddies;
		}
		
		@Override
		protected long routingTimestamp(long stamp1, long stamp2){
			return stamp1 > stamp2 ? stamp1 : -1;
		}
		
		@Override
		public void routeTriple(Tuple anchor, Action action, boolean isAdd, Graph g,
				   long timestamp) {
			try {
				this.collector.ack(anchor);
			} catch (UnsupportedOperationException e) {
			}
			
			Values vals = new Values();
			for (Component c : Component.values()) {
				switch (c) {
				case action:
					// set whether this Tuple is intended for probing or building into other SteMs
					vals.add(action);
					break;
				case isAdd:
					// insert this Tuple's value of isAdd to be passed onto subscribing Bolts.
					vals.add(isAdd);
					break;
				case graph:
					// insert the new graph into the array of Values
					vals.add(g);
					break;
				case timestamp:
					vals.add(timestamp);
					break;
				default:
					break;
				}
			}
			
			distributeToEddies(anchor, vals);
		}

		@Override
		public void routeGraph(Tuple anchor, Action action, boolean isAdd, Graph g,
							   long timestamp) {
			try {
				this.collector.ack(anchor);
			} catch (UnsupportedOperationException e) {
			}
			
			Values vals = new Values();
			for (Component c : Component.values()) {
				switch (c) {
				case action:
					// set whether this Tuple is intended for probing or building into other SteMs
					vals.add(action);
					break;
				case isAdd:
					// insert this Tuple's value of isAdd to be passed onto subscribing Bolts.
					vals.add(isAdd);
					break;
				case graph:
					// insert the new graph into the array of Values
					vals.add(g);
					break;
				case timestamp:
					vals.add(timestamp);
					break;
				default:
					break;
				}
			}
			
			distributeToEddies(anchor, vals);
		}
		
		protected abstract void distributeToEddies(Tuple anchor, Values vals);

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			for (String eddy : this.eddies)
				declarer.declareStream(eddy, new Fields( Arrays.asList( Component.strings() ) ) );
		}

	}
	
}
