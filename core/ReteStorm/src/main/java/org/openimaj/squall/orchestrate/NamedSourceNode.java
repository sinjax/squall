package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A {@link NamedSourceNode} provides a unique name for a {@link Stream}. This name should be used by builders
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
public class NamedSourceNode extends NamedNode<ISource<Stream<Context>>> {
	
	
	
	private ISource<Stream<Context>> wrapped;


	/**
	 * @param parent The {@link OrchestratedProductionSystem} which this node is a part
	 * @param name the name of the node
	 * @param strm the source of triples
	 */
	public NamedSourceNode(OrchestratedProductionSystem parent, String name, ISource<Stream<Context>> strm) {
		super(parent,name);
		this.wrapped = new WrappedContextISource(strm, this);
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
	public IFunction<Context, Context> getFunction() {
		throw new UnsupportedOperationException();
	}



	@Override
	public Initialisable getInit() {
		return this.wrapped;
	}



	@Override
	public boolean isInitialisable() {
		return true;
	}



	@Override
	public boolean isVariableHolder() {
		return false;
	}



	@Override
	public AnonimisedRuleVariableHolder getVariableHolder() {
		throw new UnsupportedOperationException();
	}



	@Override
	public boolean isOperation() {
		return false;
	}
	
	@Override
	public boolean isReentrantSource() {
		return false;
	}



	@Override
	public IOperation<Context> getOperation() {
		throw new UnsupportedOperationException();
	}


	@Override
	public ISource<Stream<Context>> getSource() {
		return wrapped;
	}


	@Override
	public ISource<Stream<Context>> getData() {
		return wrapped;
	}
}
