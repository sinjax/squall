package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.VariableFunction;
import org.openimaj.squall.orchestrate.NamedFunctionNode;
import org.openimaj.squall.orchestrate.NamedStream;
import org.openimaj.util.data.Context;

class NamedVarFunctionNode extends NamedFunctionNode{

	private VariableFunction<Context, Context> varfunc;

	public NamedVarFunctionNode(String name, VariableFunction<Context, Context> func) {
		super(name, func);
		this.varfunc = func;
	}
	
	public VariableFunction<Context, Context> getVarFunc(){
		return varfunc;
	}
	
}