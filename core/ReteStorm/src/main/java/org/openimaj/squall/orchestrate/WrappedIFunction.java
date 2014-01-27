package org.openimaj.squall.orchestrate;

import java.util.List;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.util.data.Context;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class WrappedIFunction implements IFunction<Context, Context> {

	private IFunction<Context, Context> func;
	private ContextAugmentingFunction saf;
	
	/**
	 * @param func
	 * @param nn
	 */
	public WrappedIFunction(IFunction<Context,Context> func, NamedNode<IFunction<Context,Context>> nn){
		this.saf = new ContextAugmentingFunction(ContextAugmentingFunction.NAME_KEY, nn.getName());
		this.func = func;
	}
	
	@Override
	public List<Context> apply(Context in) {
		List<Context> ret = this.func.apply(in);
		if(ret == null) return null;
		for (Context ctx : ret) {
			this.saf.apply(ctx);
		}
		return ret;
	}
	
	@Override
	public void setup() {
		func.setup();
	}

	@Override
	public void cleanup() {
		func.cleanup();
	}
	
	@Override
	public String toString() {
		return func.toString();
	}

}
