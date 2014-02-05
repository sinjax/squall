package org.openimaj.squall.compile.data;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <I>
 * @param <O>
 */
public interface SIFunction<I, O> extends IFunction<I, O>,
		BufferedOutputStreamHolder<O> {

}
