package org.openimaj.squall.orchestrate;

import java.util.List;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;
import org.openimaj.util.function.MultiFunction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class WrappedFunction implements MultiFunction<Context,Context>, KryoSerializable {

	private ContextAugmentingFunction saf;
	private MultiFunction<Context,Context> func;

	/**
	 * @param func
	 * @param nn
	 */
	public WrappedFunction(MultiFunction<Context,Context> func, NamedNode<MultiFunction<Context,Context>> nn) {
		this.saf = new ContextAugmentingFunction(ContextKey.PREV_FUNC_KEY.toString(), nn.getName());
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
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private WrappedFunction(){}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.saf);
		kryo.writeClassAndObject(output, func);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		this.saf = (ContextAugmentingFunction) kryo.readClassAndObject(input);
		this.func = (MultiFunction<Context, Context>) kryo.readClassAndObject(input);
	}
}