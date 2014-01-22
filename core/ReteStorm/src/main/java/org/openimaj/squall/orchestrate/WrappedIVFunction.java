package org.openimaj.squall.orchestrate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *@author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class WrappedIVFunction extends IVFunction<Context,Context> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2859104030344206890L;
	private ContextAugmentingFunction saf;
	private IVFunction<Context,Context> func;

	/**
	 * @param func
	 * @param nn
	 */
	public WrappedIVFunction(IVFunction<Context,Context> func, NamedNode<IVFunction<Context,Context>> nn) {
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
	public String[] variables() {
		return func.variables();
	}

	@Override
	public List<String> ruleVariables() {
		return func.ruleVariables();
	}

	@Override
	public Map<String, String> ruleToBaseVarMap() {
		return func.ruleToBaseVarMap();
	}

	@Override
	public boolean mirrorInRule(AnonimisedRuleVariableHolder toMirror) {
		return func.mirrorInRule(toMirror);
	}

	@Override
	public String identifier(Map<String, String> varmap) {
		return func.identifier(varmap);
	}

	@Override
	public Collection<AnonimisedRuleVariableHolder> contributors() {
		return func.contributors();
	}

	@Override
	public String identifier() {
		return func.identifier();
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
	private WrappedIVFunction(){}
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.saf);
		kryo.writeClassAndObject(output, this.func);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input) {
		this.saf = (ContextAugmentingFunction) kryo.readClassAndObject(input);
		this.func = (IVFunction<Context, Context>) kryo.readClassAndObject(input);
	}

	
}