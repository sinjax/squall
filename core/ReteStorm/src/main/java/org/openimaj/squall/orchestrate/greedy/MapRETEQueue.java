package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.utils.CircularPriorityWindow;
import org.openimaj.rdf.storm.utils.DeepHashArray;
import org.openimaj.rdf.storm.utils.HashedCircularPriorityWindow;
import org.openimaj.rdf.storm.utils.OverflowHandler;
import org.openimaj.squall.orchestrate.WindowInformation;

import scala.actors.threadpool.Arrays;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class MapRETEQueue{
	
//	private static final int capacity = 1000;
//	private static final long duration = 1;
//	private static final TimeUnit unit = TimeUnit.MINUTES;
	
	MapRETEQueue sibling;
	HashedCircularPriorityWindow<DeepHashArray<Node>,Map<String,Node>> window;
	List<String> sharedVariables; // must match the sibling stream
	
	/**
	 * @param sharedVariables
	 * @param wi 
	 */
	public MapRETEQueue(List<String> sharedVariables, WindowInformation wi) {
		this.sharedVariables = sharedVariables;
		
		window = new HashedCircularPriorityWindow<DeepHashArray<Node>,Map<String,Node>>(null, wi.getCapacity(), wi.getDuration(), wi.getGranularity());
	}
	
	/**
	 * @param handler 
	 * @param sharedVariables
	 * @param wi 
	 */
	public MapRETEQueue(OverflowHandler<Map<String, Node>> handler, List<String> sharedVariables, WindowInformation wi) {
		this.sharedVariables = sharedVariables;
		
		window = new HashedCircularPriorityWindow<DeepHashArray<Node>,Map<String,Node>>(handler, wi.getCapacity(), wi.getDuration(), wi.getGranularity());
	}
	
	/**
	 * @param other
	 */
	public void pair(MapRETEQueue other ){
		this.sibling = other;
		other.sibling = this;
	}
	
	/**
	 * @param typed
	 * @param timestamp 
	 * @param delay 
	 * @param unit 
	 * @return
	 */
	public List<Map<String,Node>> offer(Map<String, Node> typed, long timestamp, long delay, TimeUnit unit) {
		window.put(extractSharedBindings(typed), typed, timestamp, delay, unit);
		return check(typed);
	}
	
	/**
	 * @param typed
	 * @param timestamp 
	 * @param delay 
	 * @return
	 */
	public List<Map<String,Node>> offer(Map<String, Node> typed, long timestamp, long delay) {
		window.put(extractSharedBindings(typed), typed, timestamp, delay);
		return check(typed);
	}
	
	/**
	 * @param typed
	 * @param timestamp 
	 * @return
	 */
	public List<Map<String,Node>> offer(Map<String, Node> typed, long timestamp) {
		window.put(extractSharedBindings(typed), typed, timestamp);
		return check(typed);
	}
	
	/**
	 * @param typed
	 * @return
	 */
	public List<Map<String,Node>> offer(Map<String, Node> typed) {
		window.put(extractSharedBindings(typed), typed);
		return check(typed);
	}
	
	private DeepHashArray<Node> extractSharedBindings(Map<String, Node> binds) {
		DeepHashArray<Node> vals = new DeepHashArray<Node>(new Node[this.sharedVariables.size()]);
		int i = 0;
		for (String key : this.sharedVariables){
			Node node = binds.get(key);
			if(node.isConcrete()){
				vals.set(i++, node);
				continue;
			} else {
				throw new UnsupportedOperationException("Incorrect node type for comparison: " + node);
			}
		}
		return vals;
	}

	private List<Map<String,Node>> check(Map<String, Node> typed) {
		List<Map<String, Node>> ret = new ArrayList<Map<String,Node>>();
		DeepHashArray<Node> sharedBindings = extractSharedBindings(typed);
		List<Map<String, Node>> matchedQueue = sibling.window.getWindow(sharedBindings);
		if (matchedQueue != null){
			for (Map<String, Node> sibitem : matchedQueue) {
				Map<String,Node> newbind = new HashMap<String, Node>();
				for (Entry<String, Node> map : typed.entrySet()) {
					newbind.put(map.getKey(), map.getValue());
				}
				for (Entry<String, Node> map : sibitem.entrySet()) {
					newbind.put(map.getKey(), map.getValue());
				}
				ret.add(newbind);
			}
		}
		
		return ret ;
	}
	
}