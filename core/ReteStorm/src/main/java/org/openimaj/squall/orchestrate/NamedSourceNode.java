package org.openimaj.squall.orchestrate;

import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
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
public class NamedSourceNode extends NamedNode<NamedSourceNode> implements Stream<Context>{
	/**
	 * key used to insert this node's name into the returned context
	 */
	public static final String NAME_KEY = "information";
	private String name;
	private Function<Context, Context> func;
	private Function<Context, Context> wrapped;
	
	
	
	/**
	 * @param name the name of the node
	 * @param func
	 */
	public NamedSourceNode(String name, Function<Context, Context> func) {
		super(name);
		this.func = func;
		this.wrapped = new Function<Context, Context>() {
			
			@Override
			public Context apply(Context in) {
				Context out = NamedSourceNode.this.func.apply(in);
				addName(out);
				return out;
			}
		};
	}
	
	
	/**
	 * @param in
	 * @return the function wrapping this node's behavior
	 */
	public Context apply(Context in) {
		return wrapped.apply(in);
	}


	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Context next() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void forEach(Operation<Context> op) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void forEach(Operation<Context> operation,
			Predicate<Context> stopPredicate) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int forEach(Operation<Context> operation, int limit) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void parallelForEach(Operation<Context> op) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void parallelForEach(Operation<Context> op, ThreadPoolExecutor pool) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Stream<Context> filter(Predicate<Context> filter) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <R> Stream<R> map(Function<Context, R> mapper) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <R> Stream<R> map(MultiFunction<Context, R> mapper) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <R> Stream<R> transform(
			Function<Stream<Context>, Stream<R>> transform) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
