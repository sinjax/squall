package org.openimaj.squall.orchestrate;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A {@link NamedNode} provides a unique name for a function. This name should be used by builders
 * to guarantee that the output of a node goes to the correct children. The function provided
 * by a {@link NamedNode} takes as input a {@link Context} and returns a {@link Context}. Exactly
 * how these {@link Context} instances are transmitted is entirley the choice of the builder, but it must be 
 * guaranteed that:
 * 	- The output of a given node is transmitted to all its children
 *  - If two nodes share the same child, the same instance of the child is transmitted outputs from both nodes
 *  
 *  It is the job of the builder to guarantee consistent instances based on the {@link NamedNode}'s name
 * 
 * The {@link NamedNode} is a function itself which wraps the internal {@link Function} call
 * @param <DATA> 
 */
public abstract class NamedNode<DATA> extends DGNode<NamedNode<?>,NamedStream,DATA>{
	/**
	 * key used to insert this node's name into the returned context
	 */
	public static final String NAME_KEY = "information";
	private String name;
	/**
	 * @param name the name of the node
	 */
	public NamedNode(String name) {
		this.name = name;
	}
	
	void addName(Context c){
		c.put(NAME_KEY, NamedNode.this.name);
	}
	
	/**
	 * @return true if {@link #getSource()} will return 
	 */
	public abstract boolean isSource();
	/**
	 * @return true if {@link #getFunction()} will return
	 */
	public abstract boolean isFunction();
	
	/**
	 * @return {@link Stream} returned if this node is a Source, {@link UnsupportedOperationException} otherwise
	 */
	public abstract Stream<Context> getSource();
	/**
	 * @return {@link Function} returned if this node is not a Source, {@link UnsupportedOperationException} otherwise
	 */
	public abstract Function<Context,Context> getFunction();
}
