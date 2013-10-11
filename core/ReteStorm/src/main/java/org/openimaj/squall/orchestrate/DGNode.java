package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A node in a Directed Graph
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <NODE> The type of the node itself
 * @param <EDGE> The type of the edge connecting nodes
 * @param <DATA> The type of data held by the node
 *
 */
public abstract class DGNode<NODE extends DGNode<NODE,EDGE,?>,EDGE,DATA> implements Iterable<NODE>{
	List<NODE> children;
	List<NODE> parents;
	List<EDGE> edges;
	private DirectedGraph<NODE, EDGE> root;
	
	/**
	 * @param root 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public DGNode(DirectedGraph<NODE, EDGE> root) {
		this.children = new ArrayList<NODE>();
		this.parents = new ArrayList<NODE>();
		this.edges = new ArrayList<EDGE>();
		this.root = root;
		this.root.addVertex((NODE) this);
	}
	
	/**
	 * @param child
	 * @return the child being added
	 */
	NODE addChild(NODE child){
		this.children.add(child);
		return child;
	}
	
	@Override
	public Iterator<NODE> iterator() {
		return childiterator();
	}
	
	/**
	 * @return
	 */
	public Iterator<NODE> childiterator() {
		return this.children.iterator();
	}
	
	/**
	 * @return
	 */
	public Iterator<NODE> parentiterator() {
		return this.parents.iterator();
	}

	/**
	 * @param child
	 * @return the child being added
	 */
	NODE addParent(NODE parent){
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
	public void connect(EDGE edge, NODE child) {
		this.addChild(child);
		child.addParent((NODE) this);
		this.addEdge(edge);
		this.root.addEdge(edge, (NODE) this, child);
	}
	
	/**
	 * @return the data held by the node
	 */
	public abstract DATA getData();
	
	/**
	 * @return number of child nodes
	 */
	public int childCount() {
		return this.children.size();
	}
	
	/**
	 * @return number of parent nodes
	 */
	public int parentCount() {
		return this.parents.size();
	}
	
}
