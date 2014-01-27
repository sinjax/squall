package org.openimaj.squall.orchestrate;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <NODE>
 * @param <EDGE> 
 */
public abstract class DirectedGraph<NODE extends DGNode<NODE, EDGE, ?>,EDGE extends DirectedEdge<NODE>> {
	
	
	private HashSet<NODE> verts = new HashSet<NODE>();
	private HashSet<EDGE> edges = new HashSet<EDGE>();
	
	/**
	 * @return set of nodes with no children
	 */
	public Set<NODE> getLeaves() {
		Set<NODE> leaves = new HashSet<NODE>();
		for (EDGE edge : this.edges) {
			for (NODE node : edge.destinations()){
				if(node.outgoingEdgeCount() == 0)
				{
					leaves.add(node);
				}
			}
		}
		return leaves;
		
	}
	
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
		return edges;
	}
	
	/**
	 * @param node
	 * @return 
	 */
	public boolean addVertex(NODE node){
		if (node == null) throw new NullPointerException("Named node for connection cannot be null.");
		return this.verts.add(node);
	}
	
	/**
	 * @param edge 
	 * @return 
	 */
	public boolean addEdge(EDGE edge){
		if (edge == null) throw new NullPointerException("Directed edge for connection cannot be null.");
		return this.edges.add(edge);
	}
	
}
