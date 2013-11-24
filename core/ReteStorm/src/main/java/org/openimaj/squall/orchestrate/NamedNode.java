package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.compile.data.VariableHolder;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Source;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A {@link NamedNode} provides a unique name for a component of a production system. 
 * This name should be used by builders to guarantee that the output of a node goes to the correct children. 
 * 
 * 
 * @param <DATA> 
 */
public abstract class NamedNode<DATA> extends DGNode<NamedNode<?>,NamedStream,DATA>{
	/**
	 * key used to insert this node's name into the returned context
	 */
	public static final String NAME_KEY = "information";
	private String name;
	/**
	 * @param parent 
	 * @param name the name of the node
	 */
	public NamedNode(OrchestratedProductionSystem parent, String name) {
		super(parent);
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
	 * @return true if {@link #getVariableHolder()} will return
	 */
	public abstract boolean isVariableHolder();
	/**
	 * @return whether this node is {@link Initialisable}
	 */
	public abstract boolean isInitialisable() ;

	/**
	 * @return is this node an operation
	 */
	public abstract boolean isOperation();
	
	/**
	 * @return is the source of reentrant items
	 */
	public abstract boolean isReentrantSource();
	
	/**
	 * @return return the {@link VariableHolder} held otherwise fail horibbly
	 */
	public abstract VariableHolder getVariableHolder();
	
	/**
	 * @return {@link Stream} returned if this node is a Source, {@link UnsupportedOperationException} otherwise
	 */
	public abstract Source<Stream<Context>> getSource();
	/**
	 * @return {@link Function} returned if this node is not a Source, {@link UnsupportedOperationException} otherwise
	 */
	public abstract MultiFunction<Context,Context> getFunction();
	
	@Override
	public String toString() {
		return String.format(this.name + "(children=%d)",this.children.size());
	}

	/**
	 * @return The name of this node
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the {@link Initialisable} instance if {@link #isInitialisable()}, otherwise fail 
	 */ 
	public abstract Initialisable getInit();

	
	
	/**
	 * @return is this node an operation
	 */
	public abstract Operation<Context> getOperation();

	
}
