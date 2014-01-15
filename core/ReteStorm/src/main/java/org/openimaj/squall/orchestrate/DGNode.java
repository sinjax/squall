package org.openimaj.squall.orchestrate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A node in a Directed Graph
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <NODE> The type of the node itself
 * @param <EDGE> The type of the edge connecting nodes
 * @param <DATA> The type of data held by the node
 *
 */
public abstract class DGNode<NODE extends DGNode<NODE, EDGE, ?>, EDGE extends DirectedEdge<NODE>, DATA> implements Iterable<NODE>{
	protected Set<EDGE> childEdges;
	protected DirectedGraph<NODE, EDGE> root;
	protected Set<EDGE> parentEdges;
	
	/**
	 * @param root 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public DGNode(DirectedGraph<NODE, EDGE> root) {
		this.childEdges = new HashSet<EDGE>();
		this.parentEdges = new HashSet<EDGE>();
		this.root = root;
		this.root.addVertex((NODE) this);
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
	 * @param edge
	 * @return 
	 */
	public boolean addEdge(EDGE edge){
		return this.childEdges.add(edge);
	}
	
	/**
	 * @param edge
	 * @return 
	 */
	public boolean addIncomingEdge(EDGE edge){
		return this.parentEdges.add(edge);
	}
	
	/**
	 * @param edge
	 */
	@SuppressWarnings("unchecked")
	public void connectOutgoingEdge(EDGE edge) {
		if (!this.root.addEdge(edge)){
			for (EDGE e : this.root.edgeSet()){
				if (e.equals(edge)){
					edge = e;
					break;
				}
			}
		}
		
		if (!this.childEdges.contains(edge)){
			this.addEdge(edge);
		}
		
		edge.addSource((NODE) this);
	}
	
	/**
	 * @param edge
	 */
	@SuppressWarnings("unchecked")
	public void connectIncomingEdge(EDGE edge) {
		if (!this.root.addEdge(edge)){
			for (EDGE e : this.root.edgeSet()){
				if (e.equals(edge)){
					edge = e;
					break;
				}
			}
		}
		
		if (!this.parentEdges.contains(edge)){
			this.addIncomingEdge(edge);
		}
		
		edge.addDestination((NODE) this);
	}
	
	/**
	 * @return the data held by the node
	 */
	public abstract DATA getData();
	
	/**
	 * @return number of child nodes
	 */
	public int outgoingEdgeCount() {
		return this.childEdges.size();
	}
	
	/**
	 * @return number of parent nodes
	 */
	public int incomingEdgeCount() {
		return this.parentEdges.size();
	}
	
	/**
	 * @return the {@link DirectedGraph} this {@link DGNode} is a part of
	 */
	public DirectedGraph<NODE, EDGE> getRoot(){
		return this.root;
	}
	
	public Iterator<NODE> iterator(){
		return new Iterator<NODE>(){

			private Iterator<EDGE> edges = childEdges.iterator();
			private Iterator<NODE> dests = null;
			
			@Override
			public boolean hasNext() {
				if (this.dests == null || !this.dests.hasNext()){
					if (this.edges.hasNext()){
						this.dests = this.edges.next().destinations.iterator();
						return true;
					}
					return false;
				}
				return true;
			}

			@Override
			public NODE next() {
				return this.dests.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Cannot remove a child NODE whilst iterating");
			}
			
		};
	}
	
}
