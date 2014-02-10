package org.openimaj.squall.providers;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.providers.predicates.PredicateFunctionProvider;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * @param <IN> 
 *
 */
public abstract class FunctionRegistry<IN> {
	
	private Map<String, PredicateFunctionProvider<IN, IN>> registry;
	
	/**
	 * 
	 */
	public FunctionRegistry () {
		this.registry = new HashMap<String, PredicateFunctionProvider<IN, IN>>();
	}

	/**
	 * @param in
	 * @return
	 */
	public abstract RuleWrappedFunction<?> compile(IN in);
	
	protected PredicateFunctionProvider<IN, IN> compile(String name){
		if(!registry.containsKey(name)) throw new UnsupportedOperationException("The function provider for function '"+name+"' was not provided.");
		return this.registry.get(name);
	}
	
	/**
	 * @param name
	 * @param prov
	 */
	public void register(String name, PredicateFunctionProvider<IN, IN> prov){
		this.registry.put(name, prov);
	}

}
