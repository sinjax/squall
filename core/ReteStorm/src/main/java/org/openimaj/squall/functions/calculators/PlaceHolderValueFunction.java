package org.openimaj.squall.functions.calculators;

import java.util.List;
import java.util.Map;

import org.openimaj.squall.functions.predicates.BasePredicateFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author davidlmonks
 *
 */
@SuppressWarnings("serial")
public class PlaceHolderValueFunction extends BaseValueFunction {
	
	/**
	 * @param name
	 * @param ns
	 * @param rn
	 * @param funcmap
	 * @return
	 * @throws RIFPredicateException
	 */
	public static RuleWrappedPlaceHolderValueFunction ruleWrapped(String name, Node[] ns, Node_Variable rn, Map<Node, RuleWrappedValueFunction<?>> funcmap) throws RIFPredicateException{
		return new RuleWrappedPlaceHolderValueFunction(name, ns, rn, funcmap);
	}
	
	private String name;
	
	/**
	 * @param expr 
	 * @throws RIFPredicateException 
	 */
	protected PlaceHolderValueFunction(String name, Node[] ns,Node_Variable rn, Map<Node, BaseValueFunction> funcMap) throws RIFPredicateException {
		super(ns, rn, funcMap);
		this.name = name;
	}

	@Override
	protected List<Context> applyRoot(Context in) {
		return null;
	}
	
	private static class RuleWrappedPlaceHolderValueFunction extends RuleWrappedValueFunction<PlaceHolderValueFunction> {

		protected RuleWrappedPlaceHolderValueFunction(String fn, Node[] ns, Node_Variable rn,
				Map<Node, RuleWrappedValueFunction<?>> funcMap) throws RIFPredicateException {
			super(fn, ns, rn, funcMap);
			this.wrap(new PlaceHolderValueFunction(fn, ns, rn, this.getRulelessFuncMap()));
		}
		
	}

}
