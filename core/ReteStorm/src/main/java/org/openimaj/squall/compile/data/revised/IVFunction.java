package org.openimaj.squall.compile.data.revised;

import java.io.Serializable;

import org.openimaj.util.function.MultiFunction;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * An initialisable, variable function.
 *
 * @param <I>
 * @param <O>
 */
public interface IVFunction<I,O>  extends MultiFunction<I, O>, AnonimisedRuleVariableHolder, Initialisable, Serializable{

}
