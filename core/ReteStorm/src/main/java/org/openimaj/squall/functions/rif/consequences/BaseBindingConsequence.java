package org.openimaj.squall.functions.rif.consequences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IConsequence;
import org.openimaj.squall.compile.data.rif.AbstractRIFFunction;
import org.openimaj.squall.compile.data.rif.BindingsUtils;
import org.openimaj.util.data.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class BaseBindingConsequence extends AbstractRIFFunction implements IConsequence {

	private static final Logger logger = Logger.getLogger(BaseBindingConsequence.class);
	private String[] inVars;
	private String[] outVars;
	private String id;
	
	/**
	 * @param vars 
	 * @param ruleID 
	 */
	public BaseBindingConsequence(List<Node_Variable> vars, String ruleID){
		super();
		for (int i = 0; i < vars.size(); i ++){
			this.addVariable(vars.get(i).getName());
		}
		this.id = ruleID;
	}
	
	@Override
	public void setSourceVariableHolder(AnonimisedRuleVariableHolder arvh) {
		this.inVars = new String[this.varCount()];
		this.outVars = new String[this.varCount()];
		
		Map<String, String> subRuleToBaseVarMap = arvh.ruleToBaseVarMap();
		for (int i = 0; i < this.varCount(); i++){
			this.putRuleToBaseVarMapEntry(this.getVariable(i), subRuleToBaseVarMap.get(this.getVariable(i)));
			this.inVars[i] = subRuleToBaseVarMap.get(this.getVariable(i));
			this.outVars[i] = this.getVariable(i);
		}
	}
	
	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		Map<String,Node> ret = BindingsUtils.arrayToMap(
									BindingsUtils.mapToArray(
										bindings,
										this.inVars
									),
									this.outVars
								);
		
		Context out = new Context();
		logger.debug(this.toString());
		out.put("bindings", ret);
		out.put("rule", this.id);
		List<Context> ctxs = new ArrayList<Context>();
		ctxs.add(out);
		return ctxs;
	}

	@Override
	public String identifier() {
		// TODO
		return "";//VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause);
	}
	
	@Override
	public String identifier(Map<String, String> varmap) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("CONSEQUENCE: inVariables %s -> outVariables %s", Arrays.toString(this.inVars), Arrays.toString(this.variables()));
	}
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private BaseBindingConsequence(){}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.inVars);
		kryo.writeClassAndObject(output, this.outVars);
		output.writeString(this.id);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		this.inVars = (String[]) kryo.readClassAndObject(input);
		this.outVars = (String[]) kryo.readClassAndObject(input);
		this.id = input.readString();
	}

}
