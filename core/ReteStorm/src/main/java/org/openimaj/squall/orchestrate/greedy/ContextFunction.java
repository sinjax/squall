package org.openimaj.squall.orchestrate.greedy;

import org.openimaj.squall.compile.data.ComponentInformationFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Predicate;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A context function wraps the behaviour of a {@link Function} or {@link Predicate} as 
 * a {@link Function} that consumes and emits {@link Context} instances.
 * 
 * The idea is to encode the inputs/ouputs of the wrapped things in keys in an input/output context
 * @param <A> Input type
 * @param <B> Output type
 *
 */
public class ContextFunction<A,B> implements Function<Context, Context> {
	
	
	
	private String inkey;
	private String outkey;
	private Function<A, B> func;

	/**
	 * @param input
	 * @param output
	 * @param filter
	 */
	public ContextFunction(String input, String output, Function<A,B> filter) {
		this.inkey = input;
		this.outkey = output;
		this.func = filter;
	}

	@Override
	public Context apply(Context in) {
		A typed = in.getTyped(inkey);
		B ret = func.apply(typed);
		Context retc = new Context();
		retc.put(outkey, ret);
		return retc;
	}

	/**
	 * @param in
	 * @param out
	 * @param filter
	 * @return wrap the filter in a {@link ContextFunction} with the specified keys
	 */
	public static <A,B> ContextFunction<A, B> wrap(String in, String out, Function<A, B> filter) {
		return new ContextFunction<A,B>(in, out, filter);
	}

}
