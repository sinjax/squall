package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.JoinStream;
import org.openimaj.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

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

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(this.sources.size());
		for (int i = 0; i < this.sources.size(); i++){
			kryo.writeClassAndObject(output, this.sources.get(i));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		int size = input.readInt();
		for (int i = 0; i < size; i++){
			this.sources.add((ISource<Stream<Context>>) kryo.readClassAndObject(input));
		}
	}

	@Override
	public boolean isStateless() {
		for (ISource<Stream<Context>> source : this.sources){
			if (!source.isStateless()){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean forcedUnique() {
		for (ISource<Stream<Context>> source : this.sources){
			if (source.forcedUnique()){
				return true;
			}
		}
		return false;
	}

}
