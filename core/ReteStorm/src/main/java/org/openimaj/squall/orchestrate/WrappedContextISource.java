package org.openimaj.squall.orchestrate;

import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WrappedContextISource implements ISource<Stream<Context>>, KryoSerializable {
	
	private ContextAugmentingFunction saf;
	private ISource<Stream<Context>> strm;
	
	/**
	 * @param strm
	 * @param nn
	 */
	public WrappedContextISource(ISource<Stream<Context>> strm, NamedNode<ISource<Stream<Context>>> nn) {
		this.strm = strm;
		this.saf = new ContextAugmentingFunction(nn.getName());
	}

	@Override
	public Stream<Context> apply(Stream<Context> in) {
		return apply();
	}
	
	@Override
	public Stream<Context> apply() {
		return strm.apply().map(this.saf);
	}
	
	@Override
	public void setup() {
		strm.setup();
	}
	
	@Override
	public void cleanup() {
		strm.cleanup();
	}
	
	@Override
	public String toString() {
		return this.strm.toString();
	}
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private WrappedContextISource(){}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.saf);
		kryo.writeClassAndObject(output, this.strm);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		this.saf = (ContextAugmentingFunction) kryo.readClassAndObject(input);
		this.strm = (ISource<Stream<Context>>) kryo.readClassAndObject(input);
	}
	
}