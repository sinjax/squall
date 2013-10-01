package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A node in a {@link DirectedAcyclicGraph}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> 
 *
 */
public abstract class DAGNode<T extends DAGNode<T>> implements Iterable<T>{
	List<T> children;
	
	/**
	 * 
	 */
	public DAGNode() {
		this.children = new ArrayList<T>();
	}
	
	/**
	 * @param child
	 * @return the child being added
	 */
	public T addChild(T child){
		this.children.add(child);
		return child;
	}
	
	@Override
	public Iterator<T> iterator() {
		return this.children.iterator();
	}
}
