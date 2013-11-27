package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.utils.CircularPriorityWindow;
import org.openimaj.rdf.storm.utils.HashedCircularPriorityWindow;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class MapRETEQueue{
	MapRETEQueue sibling;
	HashedCircularPriorityWindow<Object[],Map<String,Node>> window;
	List<String> sharedVariables; // must match the sibling stream
	
	/**
	 * @param sharedVariables
	 */
	public MapRETEQueue(List<String> sharedVariables) {
		this.sharedVariables = sharedVariables;
		
		window = new HashedCircularPriorityWindow<Object[],Map<String,Node>>(null, 100, 15, TimeUnit.MINUTES);
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
	
	private Node[] extractSharedBindings(Map<String, Node> binds) {
		Node[] vals = new Node[this.sharedVariables.size()];
		int i = 0;
		for (String key : this.sharedVariables){
			Node node = binds.get(key);
			if(node.isConcrete()){
				vals[i++] = node;
				continue;
			} else {
				throw new UnsupportedOperationException("Incorrect node type for comparison: " + node);
			}
		}
		return vals;
	}

	private List<Map<String,Node>> check(Map<String, Node> typed) {
		List<Map<String, Node>> ret = new ArrayList<Map<String,Node>>();
		Queue<Map<String, Node>> matchedQueue = sibling.window.getWindow(extractSharedBindings(typed));
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