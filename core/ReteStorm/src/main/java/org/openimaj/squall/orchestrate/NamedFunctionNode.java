package org.openimaj.squall.orchestrate;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A {@link NamedFunctionNode} provides a unique name for a function. This name should be used by builders
 * to guarantee that the output of a node goes to the correct children. The function provided
 * by a {@link NamedFunctionNode} takes as input a {@link Context} and returns a {@link Context}. Exactly
 * how these {@link Context} instances are transmitted is entirley the choice of the builder, but it must be 
 * guaranteed that:
 * 	- The output of a given node is transmitted to all its children
 *  - If two nodes share the same child, the same instance of the child is transmitted outputs from both nodes
 *  
 *  It is the job of the builder to guarantee consistent instances based on the {@link NamedFunctionNode}'s name
 * 
 * The {@link NamedFunctionNode} is a function itself which wraps the internal {@link Function} call
 */
public class NamedFunctionNode extends NamedNode<Function<Context,Context>> {
	
	private Function<Context, Context> func;
	private Function<Context, Context> wrapped;
	
	
	
	/**
	 * @param name the name of the node
	 * @param func
	 */
	public NamedFunctionNode(String name, Function<Context, Context> func) {
		super(name);
		this.func = func;
		this.wrapped = new Function<Context, Context>() {
			
			@Override
			public Context apply(Context in) {
				Context out = NamedFunctionNode.this.func.apply(in);
				addName(out);
				return out;
			}
		};
	}



	@Override
	public Function<Context, Context> getData() {
		return this.wrapped;
	}
	
	
	
	
	
}
