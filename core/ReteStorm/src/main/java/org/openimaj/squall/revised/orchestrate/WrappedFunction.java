package org.openimaj.squall.revised.orchestrate;

import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
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
	private NamedNode<MultiFunction<Context, Context>> nn;
	private MultiFunction<Context,Context> func;

	/**
	 * @param func
	 * @param nn
	 */
	public WrappedFunction(MultiFunction<Context,Context> func, NamedNode<MultiFunction<Context,Context>> nn) {
		this.nn = nn;
		this.func = func;
	}
	
	@Override
	public List<Context> apply(Context in) {
		List<Context> ret = this.func.apply(in);
		if(ret == null) return null;
		for (Context ctx : ret) {
			nn.addName(ctx);
		}
		return ret;
	}
}