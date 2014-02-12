package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Source;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NGNOperation extends NamedNode<IFunction<Context, Context>>{

	private IOperation<Context> op;

	/**
	 * @param parent
	 * @param name
	 * @param func
	 */
	public NGNOperation(OrchestratedProductionSystem parent, String name, IOperation<Context> func) {
		super(parent, name);
		this.op = func;
	}
	
	public IFunction<Context, Context> getData(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSource() {
		return false;
	}

	@Override
	public boolean isFunction() {
		return false;
	}

	@Override
	public Source<Stream<Context>> getSource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IFunction<Context, Context> getFunction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Initialisable getInit() {
		return this.op;
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
		return true;
	}

	@Override
	public IOperation<Context> getOperation() {
		return this.op;
	}

	@Override
	public boolean isReentrantSource() {
		return false;
	}

	@Override
	public boolean isStateless() {
		return this.op.isStateless();
	}
	
	@Override
	public boolean forcedUnique() {
		return this.op.forcedUnique();
	}
	
}