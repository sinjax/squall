package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.data.RuleWrapped;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Source;
import org.openimaj.util.stream.Stream;

/**
 * A {@link NamedNode} that holds an {@link RuleWrapped} {@link IFunction}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * @param <T> the type of RuleWrapper held by the Named Node
 */
public class NNIVFunction<T extends RuleWrapped<? extends IFunction<Context,Context>>> extends NamedNode<T>{

	private T varFunc;
	private WrappedIFunction wrapped;

	/**
	 * @param parent
	 * @param name
	 * @param func
	 */
	public NNIVFunction(OrchestratedProductionSystem parent,
						String name,
						T func) {
		super(parent, name);
		this.varFunc = func;
		this.wrapped = new WrappedIFunction(this.varFunc.getWrapped(), (NamedNode<?>) this);
	}
	
	@Override
	public T getData() {
		return this.varFunc;
	}

	@Override
	public boolean isSource() {
		return false;
	}

	@Override
	public boolean isFunction() {
		return true;
	}

	@Override
	public Source<Stream<Context>> getSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IFunction<Context, Context> getFunction() {
		return this.wrapped;
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
		return true;
	}

	@Override
	public AnonimisedRuleVariableHolder getVariableHolder() {
		return this.varFunc;
	}

	@Override
	public boolean isOperation() {
		return false;
	}

	@Override
	public IOperation<Context> getOperation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReentrantSource() {
		return false;
	}

	@Override
	public boolean isStateless() {
		return this.wrapped.isStateless();
	}
	
	@Override
	public boolean forcedUnique() {
		return this.wrapped.forcedUnique();
	}
	
}