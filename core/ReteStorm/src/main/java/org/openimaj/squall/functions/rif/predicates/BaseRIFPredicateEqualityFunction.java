package org.openimaj.squall.functions.rif.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.functions.rif.calculators.BaseRIFValueFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class BaseRIFPredicateEqualityFunction extends BaseRIFPredicateFunction {
	private static final Logger logger = Logger.getLogger(BaseRIFPredicateFunction.class);
	
	/**
	 * Constructs a new equality-checking predicate function that filters bindings predicated on equality between the
	 * provided variables and any provided constants.  
	 * @param ns -
	 * 		The array of nodes to be compared
	 * @param funcs - 
	 * 		The map of variable nodes to the functions that generate their values
	 * @throws RIFPredicateException 
	 */
	public BaseRIFPredicateEqualityFunction(Node[] ns, Map<Node, BaseRIFValueFunction> funcs) throws RIFPredicateException{
		super(ns, funcs);
		Node val = null;
		boolean containsVar = false;
		for (Node n : ns){
			if (n.isConcrete()){
				if (val == null){
					val = (Node_Concrete) n;
				}else if (!val.sameValueAs(n)){
					throw new RIFPredicateException("RIF translator: All constants compared must be semantically equal.");
				}
			} else if (n.isVariable()){
				containsVar = true;
			}
		}
		if (!containsVar){
			throw new RIFPredicateException("RIF translator: Predicate must compare at least one variable.");
		}
	}

	@Override
	protected List<Context> applyRoot(Context in) {
		logger.debug(String.format("Context(%s) sent to Predicate(eq%s)" , in, Arrays.toString(super.nodes)));
		Map<String,Node> bindings = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		
		List<Context> ret = new ArrayList<Context>();
		int i = 0;
		Object match = super.extractBinding(bindings, super.nodes[i]);
		for (i++; i < super.nodes.length; i++){
			if (!match.equals(super.extractBinding(bindings, super.nodes[i]))){
				return ret;
			}
		}
		ret.add(in);
		
		logger.debug(String.format("Context(%s) passed Predicate(eq%s)" , in, Arrays.toString(super.nodes)));
		return ret;
	}

}
