package org.openimaj.squall.functions.calculators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.VariableHolder;
import org.openimaj.squall.functions.predicates.BasePredicateFunction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class BaseValueFunction extends BasePredicateFunction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8356260243919339679L;
	private Node_Variable result;
	
	/**
	 * @param ns
	 * @param rn 
	 * @param funcs 
	 * @throws RIFPredicateException
	 */
	public BaseValueFunction(Node[] ns, Node_Variable rn, Map<Node, BaseValueFunction> funcs) throws RIFPredicateException {
		super(ns, funcs);
		this.result = rn;
	}
	
	@Override
	public int mapNodeVarNames(Map<String, String> directVarMap) {
		int varIndex = super.mapNodeVarNames(directVarMap);
		String anonVarName = Integer.toString(varIndex);
		while (directVarMap.containsValue(anonVarName)){
			++varIndex;
			anonVarName = Integer.toString(varIndex);
		}
		if (directVarMap.containsKey(this.result.getName())){
			this.result = (Node_Variable) NodeFactory.createVariable(directVarMap.get(this.result.getName()));
		} else {
			directVarMap.put(this.result.getName(), anonVarName);
			this.result = (Node_Variable) NodeFactory.createVariable(anonVarName);
		}
		
		return directVarMap.size();
	}
	
	/**
	 * @return
	 */
	public Node_Variable getResultVarNode(){
		if (this.result == null){
			throw new RuntimeException("No predecessor VariableHolder set with setSourceVariableHolder(), so no result variable node has been created.");
		}
		return this.result;
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		super.write(kryo, output);
		kryo.writeClassAndObject(output, this.result);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		super.read(kryo, input);
		this.result = (Node_Variable) kryo.readClassAndObject(input);
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 * @param <T>
	 */
	public static abstract class RuleWrappedValueFunction<T extends BaseValueFunction>
									extends RuleWrappedPredicateFunction<T> {
		
		private String resultName; 
		
		/**
		 * @param fn
		 * @param ns
		 * @param nr
		 * @param funcMap
		 */
		protected RuleWrappedValueFunction(String fn, Node[] ns, Node_Variable nr, Map<Node, RuleWrappedValueFunction<?>> funcMap){
			super(fn, ns, funcMap);
			this.resultName = nr.getName();
			super.addVariable(this.resultName);
			super.putRuleToBaseVarMapEntry(this.resultName, this.resultName);
		}
	
		@Override
		public boolean setSourceVariables(AnonimisedRuleVariableHolder arvh) {
			BaseValueFunction valFunc = (BaseValueFunction) super.getWrapped();
			if (super.setSourceVariables(arvh)){
				String newBaseResultName = valFunc.getResultVarNode().getName();
				super.addVariable(this.resultName);
				super.putRuleToBaseVarMapEntry(this.resultName, newBaseResultName);
				
				return true;
			}
			return false;
			
		}
		
	}
	
}
