package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

class NamedIFunctionNode extends NamedNode<IFunction<Context, Context>>{

	private IFunction<Context, Context> varfunc;

	public NamedIFunctionNode(String name, IFunction<Context, Context> func) {
		super(name);
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
	public Stream<Context> getSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Function<Context, Context> getFunction() {
		return this.varfunc;
	}
	
}