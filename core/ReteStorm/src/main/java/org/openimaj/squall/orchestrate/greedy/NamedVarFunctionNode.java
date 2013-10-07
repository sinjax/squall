package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.VariableFunction;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.util.data.Context;

class NamedVarFunctionNode extends NamedNode<VariableFunction<Context, Context>>{

	private VariableFunction<Context, Context> varfunc;

	public NamedVarFunctionNode(String name, VariableFunction<Context, Context> func) {
		super(name);
		this.varfunc = func;
	}
	
	public VariableFunction<Context, Context> getData(){
		return varfunc;
	}
	
}