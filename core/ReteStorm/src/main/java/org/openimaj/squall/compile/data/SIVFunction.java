package org.openimaj.squall.compile.data;

import java.io.Serializable;

import org.openimaj.util.function.MultiFunction;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <I>
 * @param <O>
 */
public interface SIVFunction<I, O> extends MultiInputStreamFunction<I, O>, BufferedOutputStreamHolder<O>, VariableHolder, Initialisable, Serializable {

}
