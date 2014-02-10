package org.openimaj.squall.compile.rif.providers.predicates;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.rifcore.conditions.RIFExternal;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.data.RIFConst;
import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;
import org.openimaj.util.function.Function;

import com.hp.hpl.jena.graph.Node_Concrete;

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
	
	private static String getNameFromRegistry(String name){
		if(!providers.containsKey(name)) throw new UnsupportedOperationException("The external '"+name+"' was not provided.");
		return name;
	}
	
	/**
	 * @param ext
	 * @return a function for this external
	 */
	public static RuleWrappedPredicateFunction<? extends BasePredicateFunction> compile(RIFExternalValue ext){
		String name = getNameFromRegistry(ext.getVal().getOp().getNode().getURI());
		return providers.get(name).apply(ext);
	}
	
	/**
	 * @param ext
	 * @return a function for this external
	 */
	public static RuleWrappedValueFunction<? extends BasePredicateFunction> compile(RIFExternalExpr ext){
		RIFExpr expr = ext.getExpr();
		RIFAtom command = expr.getCommand();
		RIFConst<?> op = command.getOp();
		Node_Concrete node = op.getNode();
		String name = node.getURI();
		name = getNameFromRegistry(name);
		return providers.get(name).apply(ext);
	}
	
	/**
	 * @param name
	 * @param prov
	 */
	public static void register(String name, RIFExternalFunctionProvider prov){
		providers.put(name, prov);
	}
	
}
