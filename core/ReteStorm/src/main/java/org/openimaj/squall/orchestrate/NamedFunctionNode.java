package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.data.ComponentInformationFunction;
import org.openimaj.squall.data.ComponentInformation;
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
 * A {@link NamedFunctionNode} has a {@link ComponentInformation} instances and . The {@link NamedFunctionNode} is a
 * function itself which wraps the internal {@link Function} call while embedding its own
 * name in the {@link Context} instance under the key NAME_KEY
 */
public class NamedFunctionNode extends DAGNode<NamedFunctionNode>{
	/**
	 * key used to insert this node's name into the returned context
	 */
	public static final String INFORMATION_KEY = "information";
	private String name;
	private ComponentInformation information;
	private Function<Context, Context> func;
	
	
	/**
	 * @param name the name of the node
	 * @param cif
	 */
	public NamedFunctionNode(String name, ComponentInformationFunction<Context,Context> cif) {
		this.information = cif.information();
		this.func = cif;
		this.name = name;
	}
	/**
	 * @param name the name of the node
	 * @param information
	 * @param func
	 */
	public NamedFunctionNode(String name, ComponentInformation information, Function<Context, Context> func) {
		this.information = information;
		this.func = func;
		this.name = name;
	}
	
	
	/**
	 * @param in
	 * @return the function wrapping this node's behavior
	 */
	public Function<Context,Context> apply(Context in) {
		return new Function<Context, Context>() {
			
			@Override
			public Context apply(Context in) {
				Context out = func.apply(in);
				out.put(INFORMATION_KEY, information);
				return out;
			}
		};
	}
	
	/**
	 * @return the component information of this node
	 */
	public ComponentInformation information(){
		return this.information;
	}
	public Function<Context, Context> getFunction() {
		return this.func;
	}
	

}
