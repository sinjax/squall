package org.openimaj.squall.compile.rif.provider;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * @param <IN> 
 *
 */
public abstract class FunctionRegistry<IN> { 
	
	private Map<String, FunctionProvider<IN,IN>> registry;
	
	/**
	 * 
	 */
	public FunctionRegistry () {
		this.registry = new HashMap<String, FunctionProvider<IN,IN>>();
	}

	/**
	 * @param in
	 * @return
	 */
	public abstract IVFunction<Context,Context> compile(IN in);
	
	protected FunctionProvider<IN,IN> compile(String name){
		if(!registry.containsKey(name)) throw new UnsupportedOperationException("The function provider for function '"+name+"' was not provided.");
		return this.registry.get(name);
	}
	
	/**
	 * @param name
	 * @param prov
	 */
	public void register(String name, FunctionProvider<IN,IN> prov){
		this.registry.put(name, prov);
	}

}
