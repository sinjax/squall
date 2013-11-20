package org.openimaj.squall.compile.rif.provider;

import org.openimaj.rif.conditions.RIFExternal;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * A function which given a {@link RIFExternal} can provide a working implementation
 * of that function.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class ExternalFunctionProvider implements Function<RIFExternal, IVFunction<Context, Context>>{
	
	
	@Override
	public IVFunction<Context, Context> apply(RIFExternal in) {
		if(in instanceof RIFExternalExpr) return apply((RIFExternalExpr)in);
		else return apply((RIFExternalValue)in);
	}
	/**
	 * @param in
	 * @return
	 */
	public abstract IVFunction<Context, Context> apply(RIFExternalExpr in) ;
	
	/**
	 * @param in
	 * @return
	 */
	public abstract IVFunction<Context, Context> apply(RIFExternalValue in) ;
}
