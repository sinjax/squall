package org.openimaj.squall.functions.rif.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class LiteralNotEqualFunction extends BasePredicateFunction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2256444262394116745L;
	private static final Logger logger = Logger.getLogger(LiteralNotEqualFunction.class);
	
	/**
	 * @param ns
	 * @param funcMap
	 * @return
	 * @throws RIFPredicateException
	 */
	public static RuleWrappedLiteralNotEqualFunction ruleWrapped(
														Node[] ns,
														Map<Node, RuleWrappedValueFunction<?>> funcMap
													) throws RIFPredicateException{
		return new RuleWrappedLiteralNotEqualFunction(ns, funcMap);
	}
	
	/**
	 * @param ns
	 * @param funcMap 
	 * @throws RIFPredicateException
	 */
	public LiteralNotEqualFunction(Node[] ns, Map<Node, BaseValueFunction> funcMap) throws RIFPredicateException {
		super(ns, funcMap);
		Node val = null;
		boolean containsVar = false;
		for (Node n : ns){
			if (n.isConcrete()){
				if (val == null){
					val = (Node_Concrete) n;
				}else if (val.sameValueAs(n)){
					throw new RIFPredicateException("RIF translator: All constants compared must be semantically different.");
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
	public List<Context> applyRoot(Context in){
		logger .debug(String.format("Context(%s) sent to Predicate(neq(%s))" , in, Arrays.toString(super.nodes)));
		Map<String,Node> binds = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		
		List<Context> ret = new ArrayList<Context>();
		int i = 0;
		Object match = super.extractBinding(binds, i);
		for (i++; i < super.nodes.length; i++){
			if (match.equals(super.extractBinding(binds, i))){
				return ret;
			}
		}
		ret.add(in);
		
		logger.debug(String.format("Context(%s) passed Predicate(eq%s)" , in, Arrays.toString(super.nodes)));
		return ret;
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static class RuleWrappedLiteralNotEqualFunction extends RuleWrappedPredicateFunction<LiteralNotEqualFunction> {

		protected RuleWrappedLiteralNotEqualFunction(Node[] ns, Map<Node, RuleWrappedValueFunction<?>> funcMap) throws RIFPredicateException {
			super("LiteralNotEqual", ns, funcMap);
			this.wrap(new LiteralNotEqualFunction(ns, super.getRulelessFuncMap()));
		}
		
	}
	
}
