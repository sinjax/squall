package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.compile.data.VariableHolder;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

class NamedIFunctionNode extends NamedNode<IFunction<Context, Context>>{

	private IFunction<Context, Context> varfunc;

	public NamedIFunctionNode(OrchestratedProductionSystem parent, String name, IFunction<Context, Context> func) {
		super(parent, name);
		this.varfunc = func;
	}
	
	public IFunction<Context, Context> getData(){
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
	public IFunction<Context, Context> getFunction() {
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
		return false;
	}

	@Override
	public VariableHolder getVariableHolder() {
		throw new UnsupportedOperationException();
	}
	
}