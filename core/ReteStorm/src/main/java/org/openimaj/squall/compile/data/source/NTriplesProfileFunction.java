package org.openimaj.squall.compile.data.source;

import java.io.InputStream;

import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;

/**
 * Given an input 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NTriplesProfileFunction implements
		Function<InputStream, Stream<Context>> {

	@Override
	public Stream<Context> apply(InputStream in) {
		return new CollectionStream<Triple>(JenaUtils.readNTriples(in)).map(new ContextWrapper("triple"));
	}

}
