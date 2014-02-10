package org.openimaj.squall.providers;

import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.util.function.Function;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <IN>
 * @param <REG> 
 */
public interface FunctionProvider<IN> extends Function<IN, RuleWrappedFunction<?>> {
	
}
