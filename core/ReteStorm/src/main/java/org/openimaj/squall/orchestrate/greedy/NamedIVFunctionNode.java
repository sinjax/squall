package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.VariableFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

class NamedIVFunctionNode extends NamedNode<IVFunction<Context, Context>>{

	private IVFunction<Context, Context> varfunc;

	public NamedIVFunctionNode(String name, IVFunction<Context, Context> func) {
		super(name);
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
	public Stream<Context> getSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Function<Context, Context> getFunction() {
		return this.varfunc;
	}
	
}