package org.openimaj.squall.orchestrate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openimaj.util.pair.Pair;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <NODE>
 * @param <EDGE>
 * @param <DATA>
 */
public abstract class DirectedGraph<NODE extends DGNode<NODE, EDGE, ?>,EDGE> {
	
	
	private HashSet<NODE> verts = new HashSet<NODE>();
	private HashMap<EDGE,Pair<NODE>> edges = new HashMap<EDGE, Pair<NODE>>();
	
	/**
	 * @return the verticies of this graph
	 */
	public Set<NODE> vertexSet() {
		return verts;
	}
	
	/**
	 * @return the verticies of this graph
	 */
	public Set<EDGE> edgeSet() {
		return edges.keySet();
	}
	
	/**
	 * @param node
	 */
	public void addVertex(NODE node){
		this.verts.add(node);
	}
	
	/**
	 * @param edge 
	 * @param start 
	 * @param end 
	 */
	public void addEdge(EDGE edge, NODE start, NODE end){
		this.edges.put(edge,new Pair<NODE>(start,end));
	}
	
	/**
	 * @param e
	 * @return the start of an edge
	 */
	public NODE getEdgeSource(EDGE e) {
		return this.edges.get(e).firstObject();
	}
	
	/**
	 * @param e
	 * @return the start of an edge
	 */
	public NODE getEdgeTarget(EDGE e) {
		return this.edges.get(e).secondObject();
	}
	
}
