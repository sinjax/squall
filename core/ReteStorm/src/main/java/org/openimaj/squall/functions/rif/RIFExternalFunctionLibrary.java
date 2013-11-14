package org.openimaj.squall.functions.rif;

import org.openimaj.rif.conditions.data.RIFExpr;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

/**
 * A library of IVFunctions providing the functionality of some system/language's builtin functions.
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public interface RIFExternalFunctionLibrary {

	/**
	 * Constructs and returns a streaming implementation of the function called in the provided {@link RIFExternalExpr}.
	 * @param expr
	 * 		The {@link RIFExternalExpr} wrapping the function call.
	 * @return
	 * 		The {@link IVFunction} that emulates the behaviour of the called function.
	 */
	public IVFunction<Context, Context> compile(RIFExpr expr);
	
	/**
	 * Constructs and returns a streaming implementation of the function called in the provided {@link RIFExternalValue}.
	 * @param expr
	 * 		The {@link RIFExternalValue} wrapping the function call.
	 * @return
	 * 		The {@link IVFunction} that emulates the behaviour of the called function.
	 */
	public IVFunction<Context, Context> compile(RIFExternalValue expr);

}
