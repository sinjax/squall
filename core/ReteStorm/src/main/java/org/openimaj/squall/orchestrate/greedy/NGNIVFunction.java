package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.compile.data.VariableHolder;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;

class NGNIVFunction extends NamedNode<IVFunction<Context, Context>>{

	private IVFunction<Context, Context> varfunc;

	public NGNIVFunction(OrchestratedProductionSystem parent, String name, IVFunction<Context, Context> func) {
		super(parent, name);
		this.varfunc = func;
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
	public IStream<Context> getSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IVFunction<Context, Context> getFunction() {
		return this.varfunc;
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
		return this.varfunc;
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