package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Source;
import org.openimaj.util.stream.Stream;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class NNIFunction extends NamedNode<IFunction<Context, Context>> {

	private IFunction<Context, Context> varfunc;
	private IFunction<Context, Context> wrapped;

	/**
	 * @param parent
	 * @param name
	 * @param func
	 */
	public NNIFunction(OrchestratedProductionSystem parent, String name, IFunction<Context, Context> func) {
		super(parent, name);
		this.varfunc = func;
		this.wrapped = new WrappedIFunction(func, this);
	}
	
	public IFunction<Context, Context> getData(){
		return this.varfunc;
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
