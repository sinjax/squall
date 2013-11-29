package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.JoinStream;
import org.openimaj.util.stream.Stream;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class CachedCombinedISource implements ISource<Stream<Context>> {
	
	private List<ISource<Stream<Context>>> sources;
	
	/**
	 * 
	 */
	public CachedCombinedISource() {
		this.sources = new ArrayList<ISource<Stream<Context>>>();
	}
	
	@Override
	public void setup() {
		for (ISource<Stream<Context>> source : this.sources){
			source.setup();
		}
	}

	@Override
	public void cleanup() {
		for (ISource<Stream<Context>> source : this.sources){
			source.cleanup();
		}
	}

	@Override
	public Stream<Context> apply() {
		List<Stream<Context>> streams = new ArrayList<Stream<Context>>();
		for (ISource<Stream<Context>> source : this.sources){
			streams.add(source.apply());
		}
		return new JoinStream<Context>(streams);
	}

	@Override
	public Stream<Context> apply(Stream<Context> in) {
		return apply();
	}

	/**
	 * @param sourceS
	 */
	public void add(ISource<Stream<Context>> sourceS) {
		this.sources.add(sourceS);
	}

}
