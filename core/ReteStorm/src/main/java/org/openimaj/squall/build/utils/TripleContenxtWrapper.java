package org.openimaj.squall.build.utils;

import org.openimaj.util.data.Context;
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
		ctx.put("triple", in);
		return ctx ;
	}

}
