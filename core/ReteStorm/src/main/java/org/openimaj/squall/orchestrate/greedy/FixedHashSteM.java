package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.utils.DeepHashArray;
import org.openimaj.rdf.storm.utils.HashedCircularPriorityWindow;
import org.openimaj.rdf.storm.utils.OverflowHandler;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class FixedHashSteM implements TimestampedSteM<Map<String, Node>>{
	
	HashedCircularPriorityWindow<DeepHashArray<Node>,Map<String,Node>> window;
	List<String> sharedVariables; // must match the sibling stream
	
	/**
	 * @param sharedVariables
	 * @param wi 
	 */
	public FixedHashSteM(List<String> sharedVariables, WindowInformation wi) {
		this(null, sharedVariables, wi);
	}
	
	/**
	 * @param handler 
	 * @param sharedVariables
	 * @param wi 
	 */
	public FixedHashSteM(OverflowHandler<Map<String, Node>> handler, List<String> sharedVariables, WindowInformation wi) {
		this.sharedVariables = sharedVariables;
		
		window = new HashedCircularPriorityWindow<DeepHashArray<Node>,Map<String,Node>>(handler, wi.getCapacity(), wi.getDuration(), wi.getGranularity());
	}
	
	@Override
	public boolean build(Map<String, Node> typed, long timestamp, long delay, TimeUnit unit) {
		return window.put(extractSharedBindings(typed), typed, timestamp, delay, unit) != null;
	}
	
	@Override
	public boolean build(Map<String, Node> typed, long timestamp, long delay) {
		return window.put(extractSharedBindings(typed), typed, timestamp, delay) != null;
	}
	
	@Override
	public boolean build(Map<String, Node> typed, long timestamp) {
		return window.put(extractSharedBindings(typed), typed, timestamp) != null;
	}
	
	@Override
	public boolean build(Map<String, Node> typed) {
		return window.put(extractSharedBindings(typed), typed) != null;
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

	@Override
	public List<Map<String,Node>> probe(Map<String, Node> typed) {
		List<Map<String, Node>> ret = new ArrayList<Map<String,Node>>();
		DeepHashArray<Node> sharedBindings = extractSharedBindings(typed);
		List<Map<String, Node>> matchedQueue = this.window.getWindow(sharedBindings);
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