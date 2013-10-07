package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A node in a Directed Graph
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T> 
 *
 */
public abstract class DGNode<T extends DGNode<T,EDGE>,EDGE> implements Iterable<T>{
	List<T> children;
	List<T> parents;
	List<EDGE> edges;
	
	/**
	 * 
	 */
	public DGNode() {
		this.children = new ArrayList<T>();
		this.parents = new ArrayList<T>();
	}
	
	/**
	 * @param child
	 * @return the child being added
	 */
	T addChild(T child){
		this.children.add(child);
		return child;
	}
	
	@Override
	public Iterator<T> iterator() {
		return childiterator();
	}
	
	/**
	 * @return
	 */
	public Iterator<T> childiterator() {
		return this.children.iterator();
	}
	
	/**
	 * @return
	 */
	public Iterator<T> parentiterator() {
		return this.parents.iterator();
	}

	/**
	 * @param child
	 * @return the child being added
	 */
	T addParent(T parent){
		this.parents.add(parent);
		return parent;
	}
	
	void addEdge(EDGE edge){
		this.edges.add(edge);
	}
	
	/**
	 * @param edge
	 * @param child
	 */
	@SuppressWarnings("unchecked")
	public void connect(EDGE edge, T child) {
		this.addChild(child);
		child.addParent((T) this);
		this.addEdge(edge);
	}
	
}
