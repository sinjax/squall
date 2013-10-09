package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.VariableFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

class NamedVarFunctionNode extends NamedNode<VariableFunction<Context, Context>>{

	private VariableFunction<Context, Context> varfunc;

	public NamedVarFunctionNode(String name, VariableFunction<Context, Context> func) {
		super(name);
		this.varfunc = func;
	}
	
	public VariableFunction<Context, Context> getData(){
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