package org.openimaj.squall.orchestrate;

import java.util.HashSet;
import java.util.Set;

import org.openimaj.squall.compile.data.VariableHolder;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * @param <NODE> 
 *
 */
public abstract class DirectedEdge<NODE extends DGNode<NODE, ? extends DirectedEdge<NODE>, ?>> extends VariableHolder {

	protected Set<NODE> sources;
	protected Set<NODE> destinations;
	
	/**
	 * 
	 */
	public DirectedEdge(){
		this.sources = new HashSet<NODE>();
		this.destinations = new HashSet<NODE>();
	}
	
	/**
	 * @param source
	 * @return
	 */
	public DirectedEdge<NODE> addSource(NODE source){
		this.sources.add(source);
		return this;
	}
	
	/**
	 * @return
	 */
	public Iterable<NODE> sources(){
		return this.sources;
	}
	
	/**
	 * @param dest 
	 * @return
	 */
	public DirectedEdge<NODE> addDestination(NODE dest){
		this.destinations.add(dest);
		return this;
	}
	
	/**
	 * @return
	 */
	public Iterable<NODE> destinations(){
		return this.destinations;
	}

}
