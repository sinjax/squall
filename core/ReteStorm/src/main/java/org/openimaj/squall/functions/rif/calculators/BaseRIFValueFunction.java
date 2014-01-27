package org.openimaj.squall.functions.rif.calculators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.VariableHolder;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction;

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
public abstract class BaseRIFValueFunction extends BaseRIFPredicateFunction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8356260243919339679L;
	private Node_Variable result;
	private String ruleVarName;
	
	/**
	 * @param ns
	 * @param rn 
	 * @throws RIFPredicateException
	 */
	public BaseRIFValueFunction(Node[] ns, Node_Variable rn) throws RIFPredicateException {
		super(ns);
		this.ruleVarName = rn.getName();
	}
	
	@Override
	public void setSourceVariableHolder(AnonimisedRuleVariableHolder arvh) {
		super.setSourceVariableHolder(arvh);
		String anonVarName = Integer.toString(arvh.varCount() + 1);
		this.result = (Node_Variable) NodeFactory.createVariable(anonVarName);
		super.addVariable(anonVarName);
		super.putRuleToBaseVarMapEntry(this.ruleVarName, anonVarName);
		this.ruleVarName = null;
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
	
}
