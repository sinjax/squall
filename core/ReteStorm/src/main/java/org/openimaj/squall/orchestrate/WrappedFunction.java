package org.openimaj.squall.orchestrate;

import java.util.List;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.MultiFunction;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class WrappedFunction implements MultiFunction<Context,Context>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1533515752338530090L;
	private ContextAugmentingFunction saf;
	private MultiFunction<Context,Context> func;

	/**
	 * @param func
	 * @param nn
	 */
	public WrappedFunction(MultiFunction<Context,Context> func, NamedNode<MultiFunction<Context,Context>> nn) {
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
}