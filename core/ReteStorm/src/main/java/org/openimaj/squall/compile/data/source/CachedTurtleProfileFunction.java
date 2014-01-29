package org.openimaj.squall.compile.data.source;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.jena.riot.Lang;
import org.apache.log4j.Logger;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;
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
public class CachedTurtleProfileFunction implements Function<InputStream, Stream<Context>> {
	
	private static final Logger logger = Logger.getLogger(CachedTurtleProfileFunction.class);
	private final class TurtleStream extends AbstractStream<Context> {
		private ArrayList<Triple> tripleCol;
		private Iterator<Triple> iter;

		public TurtleStream(InputStream in) {
			Iterator<Triple> iter = JenaUtils.createIterator(in, Lang.TURTLE);
			this.tripleCol = new ArrayList<Triple>();
			logger.debug("Loading all triples!");
			while(iter.hasNext()){
				this.tripleCol.add(iter.next());
			}
			logger.debug(this.tripleCol.size() + " Triples loaded from stream!");
			this.iter = this.tripleCol.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Context next() {
			Context context = new Context(ContextKey.TRIPLE_KEY.toString(),iter.next());
			return context;
		}
	}

	@Override
	public Stream<Context> apply(InputStream in) {
		return new TurtleStream(in);
	}

}
