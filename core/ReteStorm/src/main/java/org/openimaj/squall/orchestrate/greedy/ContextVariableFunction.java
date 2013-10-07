package org.openimaj.squall.orchestrate.greedy;

import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.VariableFunction;
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
public class ContextVariableFunction<A,B> implements VariableFunction<Context, Context> {
	
	
	
	private String inkey;
	private String outkey;
	private VariableFunction<A, B> func;

	/**
	 * @param input
	 * @param output
	 * @param filter
	 */
	public ContextVariableFunction(String input, String output, VariableFunction<A,B> filter) {
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
	 * @return wrap the filter in a {@link ContextVariableFunction} with the specified keys
	 */
	public static <A,B> ContextVariableFunction<A, B> wrap(String in, String out, VariableFunction<A, B> filter) {
		return new ContextVariableFunction<A,B>(in, out, filter);
	}

	@Override
	public List<String> variables() {
		return func.variables();
	}

	@Override
	public String anonimised(Map<String, Integer> varmap) {
		return func.anonimised(varmap);
	}

	@Override
	public String anonimised() {
		return func.anonimised();
	}

}
