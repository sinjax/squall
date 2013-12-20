package org.openimaj.squall.revised.orchestrate;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.orchestrate.DirectedGraph;
import org.openimaj.squall.orchestrate.NamedSourceNode;
import org.openimaj.squall.orchestrate.Orchestrator;
import org.openimaj.squall.orchestrate.ReentrantNNIVFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * 
 * An orchestrated production system provides a directed graph of {@link NamedNode} instances
 * ending with a single {@link NamedNode} containing an {@link Operation} instance.
 * 
 * {@link OrchestratedProductionSystem} are returned by {@link Orchestrator}
 * 
 * These functions and operations encapsulate the behavior of a {@link CompiledProductionSystem} system
 * and provide the final step before a realised production system which can be built by a Builder.
 * 
 * The {@link NamedNode} may provide:
 * 	- A source of Context instances
 * 	- Functions against Context instances
 * 	- Operations, consumeing processed Context instances
 * 
 * Builders must decide how these different operations are called exactly, however the general flow is:
 * Sources -> Functions -> Operations and is dictated by the {@link DirectedGraph} provided in this {@link OrchestratedProductionSystem}
 * 
 * {@link NamedNode} are connected by {@link NamedStream} instances. {@link NamedStream}s provide functions which add extra
 * information to {@link Context} instances at run time. In general, Builders should apply the function of a {@link NamedStream}
 * to the {@link Context} being transmitted before handing the {@link Context} to the next {@link NamedNode} 
 * 
 * Exactly how these {@link Context} instances are transmitted is entirely the choice of the builder, but it must be 
 * guaranteed that:
 * 
 * 	- The output of a given node is transmitted to all its children
 *  - If two nodes share the same child, the same instance of the child is transmitted outputs from both nodes
 *  
 *  It is the job of the builder to guarantee consistent instances based on the {@link NamedNode}'s name
 * 
 */
public class OrchestratedProductionSystem extends DirectedGraph<NamedNode<?>,NamedStream> {
	/**
	 * The source nodes which are connected to the children
	 */
	public List<NamedSourceNode> root;
	
	/**
	 * The source nodes which are connected to the children
	 */
	public ReentrantNNIVFunction reentrant;
	
	
	/**
	 * 
	 */
	public OrchestratedProductionSystem() {
		
		root = new ArrayList<NamedSourceNode>();
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (NamedSourceNode elm : root) {
			buf.append(elm.toString());
			buf.append("\n");
		}
		return buf.toString();
	}

	

	

	
	
}
