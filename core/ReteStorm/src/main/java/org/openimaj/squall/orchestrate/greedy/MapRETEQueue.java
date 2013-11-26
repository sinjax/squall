package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openimaj.rdf.storm.utils.CircularPriorityWindow;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class MapRETEQueue{
	MapRETEQueue sibling;
	CircularPriorityWindow<Map<String,Node>> window;
	List<String> sharedVariables; // must match the sibling stream
	
	/**
	 * @param sharedVariables
	 */
	public MapRETEQueue(List<String> sharedVariables) {
		this.sharedVariables = sharedVariables;
		
		window = new CircularPriorityWindow<Map<String,Node>>(null, 100, 15, TimeUnit.MINUTES);
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
	 * @return
	 */
	public List<Map<String,Node>> offer(Map<String, Node> typed) {
		window.offer(typed);
		List<Map<String, Node>> ret = new ArrayList<Map<String,Node>>();
		for (Map<String, Node> sibitem : sibling.window) {
			boolean matchOK = true;
			for (String sharedKey : this.sharedVariables) {
				boolean nomatch = false;
				Node fromSibling = sibitem.get(sharedKey);
				Node fromThis = typed.get(sharedKey);
				try{
					nomatch = !fromSibling.matches(fromThis);
				}
				catch(Throwable t){
					t.printStackTrace();
				}
				if(nomatch){
					matchOK = false;
					break;
				}
			}
			if(matchOK){
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