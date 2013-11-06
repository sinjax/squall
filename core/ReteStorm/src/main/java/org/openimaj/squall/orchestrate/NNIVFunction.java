package org.openimaj.squall.orchestrate;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.compile.data.VariableHolder;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Source;
import org.openimaj.util.stream.Stream;

public class NNIVFunction extends NamedNode<IVFunction<Context, Context>>{

	private IVFunction<Context, Context> varfunc;
	private IVFunction<Context, Context> wrapped;

	public NNIVFunction(OrchestratedProductionSystem parent, String name, IVFunction<Context, Context> func) {
		super(parent, name);
		this.varfunc = func;
		this.wrapped = new WrappedIVFunction(func, this);
	}
	
	public IVFunction<Context, Context> getData(){
		return varfunc;
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
	public IVFunction<Context, Context> getFunction() {
		return this.wrapped;
	}

	@Override
	public Initialisable getInit() {
		return this.varfunc;
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
	public VariableHolder getVariableHolder() {
		return this.wrapped;
	}

	@Override
	public boolean isOperation() {
		return false;
	}

	@Override
	public Operation<Context> getOperation() {
		throw new UnsupportedOperationException();
	}
	
}