package org.openimaj.squall.orchestrate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * An orchestrated production system provides a directed graph of {@link Function} instances
 * ending with a single {@link Operation} instance.
 * 
 * These functions and operations encapsulate the behavior of a {@link CompiledProductionSystem} system
 * and provide the final step before a realised production system which can be built by a Builder.
 * 
 * The nodes in this graph can be either:
 * 	InputNode - Contain a {@link Function} which consume a {@link Triple} and outputs a {@link List} of {@link Map} bindings
 * 	IntermediateNode - Contain a function which consume a {@link List} of {@link Map} 
 * 	TerminalNode
 * 
 */
public class OrchestratedProductionSystem {
	/**
	 * The source nodes which are connected to the children
	 */
	public List<NamedSourceNode> root;
	/**
	 * 
	 */
	public OrchestratedProductionSystem() {
		
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
