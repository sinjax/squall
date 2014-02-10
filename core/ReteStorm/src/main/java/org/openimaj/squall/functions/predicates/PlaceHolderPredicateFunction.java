package org.openimaj.squall.functions.predicates;

import java.util.List;
import java.util.Map;

import org.openimaj.squall.functions.calculators.BaseValueFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;

/**
 * @author davidlmonks
 *
 */
@SuppressWarnings("serial")
public class PlaceHolderPredicateFunction extends BasePredicateFunction {
	
	/**
	 * @param name
	 * @param ns
	 * @param funcmap
	 * @return
	 * @throws RIFPredicateException
	 */
	public static RuleWrappedPlaceHolderPredicateFunction ruleWrapped(String name, Node[] ns, Map<Node, RuleWrappedValueFunction<?>> funcmap) throws RIFPredicateException{
		return new RuleWrappedPlaceHolderPredicateFunction(name, ns, funcmap);
	}
	
	private String name;
	
	/**
	 * @param expr 
	 * @throws RIFPredicateException 
	 */
	protected PlaceHolderPredicateFunction(String name, Node[] ns, Map<Node, BaseValueFunction> funcMap) throws RIFPredicateException {
		super(ns, funcMap);
		this.name = name;
	}
	
	@Override
	public BasePredicateFunction clone() {
		try {
			return new PlaceHolderPredicateFunction(this.name, super.getNodes(), super.getFuncMap());
		} catch (RIFPredicateException e) {
			throw new RuntimeException("Clone of valid function should not be invalid.", e);
		}
	}

	@Override
	protected List<Context> applyRoot(Context in) {
		return null;
	}
	
	private static class RuleWrappedPlaceHolderPredicateFunction extends RuleWrappedPredicateFunction<PlaceHolderPredicateFunction> {

		protected RuleWrappedPlaceHolderPredicateFunction(String fn, Node[] ns,
				Map<Node, RuleWrappedValueFunction<?>> funcMap) throws RIFPredicateException {
			super(fn, ns, funcMap);
			this.wrap(new PlaceHolderPredicateFunction(fn, ns, this.getRulelessFuncMap()));
		}
		
	}

}
