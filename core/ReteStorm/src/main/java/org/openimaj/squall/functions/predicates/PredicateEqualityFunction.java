package org.openimaj.squall.functions.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.functions.calculators.BaseValueFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class PredicateEqualityFunction extends BasePredicateFunction {
	
	private static final Logger logger = Logger.getLogger(BasePredicateFunction.class);
	
	/**
	 * @param ns
	 * @param funcMap
	 * @return
	 * @throws RIFPredicateException 
	 */
	public static RuleWrappedEqualityFunction ruleWrapped(
													Node[] ns,
													Map<Node, RuleWrappedValueFunction<?>> funcMap
												) throws RIFPredicateException{
		return new RuleWrappedEqualityFunction(ns, funcMap);
	}
	
	/**
	 * Constructs a new equality-checking predicate function that filters bindings predicated on equality between the
	 * provided variables and any provided constants.  
	 * @param ns -
	 * 		The array of nodes to be compared
	 * @param funcs - 
	 * 		The map of variable nodes to the functions that generate their values
	 * @throws RIFPredicateException 
	 */
	public PredicateEqualityFunction(Node[] ns, Map<Node, BaseValueFunction> funcs) throws RIFPredicateException{
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
		logger.debug(String.format("Context(%s) sent to Predicate(eq%s)" , in, Arrays.toString(super.getNodes())));
		Map<String,Node> bindings = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		
		List<Context> ret = new ArrayList<Context>();
		int i = 0;
		Object match = super.extractBinding(bindings, i);
		for (i++; i < super.getNodeCount(); i++){
			if (!match.equals(super.extractBinding(bindings, i))){
				return ret;
			}
		}
		ret.add(in);
		
		logger.debug(String.format("Context(%s) passed Predicate(eq%s)" , in, Arrays.toString(super.getNodes())));
		return ret;
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static class RuleWrappedEqualityFunction extends RuleWrappedPredicateFunction<PredicateEqualityFunction> {

		protected RuleWrappedEqualityFunction(Node[] ns, Map<Node, RuleWrappedValueFunction<?>> funcMap) throws RIFPredicateException {
			super("equal", ns, funcMap);
			this.wrap(new PredicateEqualityFunction(ns, super.getRulelessFuncMap()));
		}
		
	}

}
