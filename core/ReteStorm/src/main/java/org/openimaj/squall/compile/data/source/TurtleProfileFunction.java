package org.openimaj.squall.compile.data.source;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.jena.riot.Lang;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;

/**
 * Given an input 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TurtleProfileFunction implements Function<InputStream, Stream<Context>> {

	private final class TurtleStream extends AbstractStream<Context> {
		
		private Iterator<Triple> iter;

		public TurtleStream(InputStream in) {
			this.iter = JenaUtils.createIterator(in, Lang.TURTLE);
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Context next() {
			return new Context("triple",iter.next());
		}
	}

	@Override
	public Stream<Context> apply(InputStream in) {
		return new TurtleStream(in);
	}

}
