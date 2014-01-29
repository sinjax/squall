package org.openimaj.squall.build.utils;

import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;
import org.openimaj.util.function.Function;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TripleContenxtWrapper implements Function<Triple,Context> {

	@Override
	public Context apply(Triple in) {
		Context ctx = new Context();
		ctx.put(ContextKey.TRIPLE_KEY.toString(), in);
		return ctx ;
	}

}
