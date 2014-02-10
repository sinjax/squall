package org.openimaj.squall.functions.sources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.openimaj.squall.data.ISource;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;
import org.openimaj.util.data.ContextWrapper;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class NTriplesISourceFactory extends ISourceFactory {

	@Override
	public ISource<Stream<Context>> createSource(URI location) {
		return new ISource<Stream<Context>>() {
			
			private String nTripleStreamLocation;
			private InputStream nTripleStream;

			@Override
			public Stream<Context> apply(Stream<Context> in) {
				return apply();
			}
			
			@Override
			public Stream<Context> apply() {
				return new CollectionStream<Triple>(JenaUtils.readNTriples(nTripleStream))
						.map(new ContextWrapper(ContextKey.TRIPLE_KEY.toString()));
//				return null;
			}
			
			@Override
			public void setup() { 
				this.nTripleStream = ISource.class.getResourceAsStream(this.nTripleStreamLocation);
			}
			
			@Override
			public void cleanup() {
				try {
					this.nTripleStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.nTripleStream = null;
			}
			
			public ISource<Stream<Context>> setInputStreamSource(URI loc){
				this.nTripleStreamLocation = loc.toString();
				return this;
			}

			@Override
			public void write(Kryo kryo, Output output) {
				output.writeString(this.nTripleStreamLocation);
			}

			@Override
			public void read(Kryo kryo, Input input) {
				this.nTripleStreamLocation = input.readString();
			}

			@Override
			public boolean isStateless() {
				return false;
			}

			@Override
			public boolean forcedUnique() {
				return true;
			}
		}
		// Set the URI of the source before returning
		.setInputStreamSource(location);
	}

}
