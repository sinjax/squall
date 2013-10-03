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
	List<T> parents;
	
	/**
	 * 
	 */
	public DAGNode() {
		this.children = new ArrayList<T>();
		this.parents = new ArrayList<T>();
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
		return childiterator();
	}
	
	private Iterator<T> childiterator() {
		return this.children.iterator();
	}
	
	private Iterator<T> parentiterator() {
		return this.parents.iterator();
	}

	/**
	 * @param child
	 * @return the child being added
	 */
	public T addParent(T parent){
		this.parents.add(parent);
		return parent;
	}
	
	/**
	 * @param A
	 * @param B
	 */
	public static <T extends DAGNode<T>>void link(T A, T B){
		A.addChild(B);
		B.addParent(A);
	}
	
}
