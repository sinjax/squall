package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.data.IStream;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A {@link NamedSourceNode} provides a unique name for a function. This name should be used by builders
 * to guarantee that the output of a node goes to the correct children. The function provided
 * by a {@link NamedSourceNode} takes as input a {@link Context} and returns a {@link Context}. Exactly
 * how these {@link Context} instances are transmitted is entirley the choice of the builder, but it must be 
 * guaranteed that:
 * 	- The output of a given node is transmitted to all its children
 *  - If two nodes share the same child, the same instance of the child is transmitted outputs from both nodes
 *  
 *  It is the job of the builder to guarantee consistent instances based on the {@link NamedSourceNode}'s name
 * 
 * The {@link NamedSourceNode} is a function itself which wraps the internal {@link Function} call
 */
public class NamedSourceNode extends NamedNode<IStream<Context>> {
	
	
	
	private IStream<Context> wrapped;



	/**
	 * @param name the name of the node
	 * @param strm the source of triples
	 */
	public NamedSourceNode(String name, IStream<Context> strm) {
		super(name);
		this.wrapped = strm.map(new Function<Context, Context>() {
			
			@Override
			public Context apply(Context in) {
				addName(in);
				return in;
			}
		});
	}



	@Override
	public IStream<Context> getData() {
		return wrapped;
	}



	@Override
	public boolean isSource() {
		return true;
	}



	@Override
	public boolean isFunction() {
		return false;
	}



	@Override
	public Stream<Context> getSource() {
		return this.wrapped;
	}



	@Override
	public Function<Context, Context> getFunction() {
		throw new UnsupportedOperationException();
	}
	
	
	
}
