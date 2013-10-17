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
	List<EDGE> childEdges;
	private DirectedGraph<NODE, EDGE> root;
	private ArrayList<EDGE> parentEdges;
	
	/**
	 * @param root 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public DGNode(DirectedGraph<NODE, EDGE> root) {
		this.children = new ArrayList<NODE>();
		this.parents = new ArrayList<NODE>();
		this.childEdges = new ArrayList<EDGE>();
		this.parentEdges = new ArrayList<EDGE>();
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
	private Iterator<NODE> childiterator() {
		return this.children.iterator();
	}
	
	/**
	 * @return
	 */
	private Iterator<NODE> parentiterator() {
		return this.parents.iterator();
	}
	
	/**
	 * @return iterable of the parents
	 */
	public Iterable<NODE> parents(){
		return new Iterable<NODE>() {

			@Override
			public Iterator<NODE> iterator() {
				return parentiterator();
			}
		};
	}
	
	/**
	 * @return the edges going this Node's children
	 */
	public Iterable<EDGE> childEdges() {
		return new Iterable<EDGE>() {

			@Override
			public Iterator<EDGE> iterator() {
				return childEdges.iterator();
			}
		};
	}
	
	/**
	 * @return the edges going this Node's children
	 */
	public Iterable<EDGE> parentEdges() {
		return new Iterable<EDGE>() {

			@Override
			public Iterator<EDGE> iterator() {
				return parentEdges.iterator();
			}
		};
	}
	
	/**
	 * @return iterable of the parents
	 */
	public Iterable<NODE> children(){
		return new Iterable<NODE>() {

			@Override
			public Iterator<NODE> iterator() {
				return childiterator();
			}
		};
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
		this.childEdges.add(edge);
	}
	
	void addParentEdge(EDGE edge){
		this.parentEdges.add(edge);
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
		child.addParentEdge(edge);
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
	
	/**
	 * @return the {@link DirectedGraph} this {@link DGNode} is a part of
	 */
	public DirectedGraph<NODE, EDGE> getRoot(){
		return this.root;
	}
	
}
