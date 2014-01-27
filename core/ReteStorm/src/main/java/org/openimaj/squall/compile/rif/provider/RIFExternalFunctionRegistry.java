package org.openimaj.squall.compile.rif.provider;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.rifcore.conditions.RIFExternal;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * The external function registry holds a set of registered
 * ExternalFunctionProvider instances. The registry can be used to
 * register new providers and compile any {@link RIFExternal} to
 * a {@link Function} instance.
 *  
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks (dm11g08@ecs.soton.ac.uk)
 *
 */
public class RIFExternalFunctionRegistry {
	
	private final static Map<String, RIFExternalFunctionProvider> providers = new HashMap<String, RIFExternalFunctionProvider>();
	
	/**
	 * @param ext
	 * @return a function for this external
	 */
	public static IVFunction<Context,Context> compile(RIFExternal ext){
		String name = getOp(ext);
		if(!providers.containsKey(name)) throw new UnsupportedOperationException("The external '"+name+"' was not provided.");
		return providers.get(name).apply(ext);
	}
	
	/**
	 * @param name
	 * @param prov
	 */
	public static void register(String name, RIFExternalFunctionProvider prov){
		providers.put(name, prov);
	}

	private static String getOp(RIFExternal ext) {
		if(ext instanceof RIFExternalExpr){
			RIFExternalExpr exp = (RIFExternalExpr) ext;
			return exp.getExpr().getCommand().getOp().getNode().getURI();
		} else if (ext instanceof RIFExternalValue){
			RIFExternalValue exp = (RIFExternalValue) ext;
			return exp.getVal().getOp().getNode().getURI();
		} else {
			throw new UnsupportedOperationException("Unrecognised RIFExternal");
		}
	}
	
}
