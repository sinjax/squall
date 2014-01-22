package org.openimaj.squall.orchestrate;

import java.util.List;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.util.data.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class WrappedIFunction implements IFunction<Context, Context>, KryoSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -928471784095716730L;
	private IFunction<Context, Context> func;
	private ContextAugmentingFunction saf;
	
	/**
	 * @param func
	 * @param nn
	 */
	public WrappedIFunction(IFunction<Context,Context> func, NamedNode<IFunction<Context,Context>> nn){
		this.saf = new ContextAugmentingFunction(nn.getName());
		this.func = func;
	}
	
	@Override
	public List<Context> apply(Context in) {
		List<Context> ret = this.func.apply(in);
		if(ret == null) return null;
		for (Context ctx : ret) {
			this.saf.apply(ctx);
		}
		return ret;
	}
	
	@Override
	public void setup() {
		func.setup();
	}

	@Override
	public void cleanup() {
		func.cleanup();
	}
	
	@Override
	public String toString() {
		return func.toString();
	}
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private WrappedIFunction(){}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.saf);
		kryo.writeClassAndObject(output, this.func);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		this.saf = (ContextAugmentingFunction) kryo.readClassAndObject(input);
		this.func = (IFunction<Context, Context>) kryo.readClassAndObject(input);
	}

}
