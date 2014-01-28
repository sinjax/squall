package org.openimaj.squall.data;


import java.io.Serializable;

import org.openimaj.squall.compile.data.Initialisable;
import org.openimaj.squall.compile.data.Parallelisable;
import org.openimaj.util.function.Source;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * An initialisable source
 *
 * @param <T>
 */
public interface ISource<T> extends Source<T>, Initialisable, Parallelisable {

}
