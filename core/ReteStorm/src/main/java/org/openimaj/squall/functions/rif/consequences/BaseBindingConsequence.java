package org.openimaj.squall.functions.rif.consequences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.rif.BindingsUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class BaseBindingConsequence implements IVFunction<Context,Context> {

	private String[] inVariables;
	private String[] outVariables;
	private String id;
	
	/**
	 * @param vars 
	 * @param ruleID 
	 */
	public BaseBindingConsequence(List<Node_RuleVariable> vars, String ruleID){
		this.inVariables = new String[vars.size()];
		this.outVariables = new String[this.inVariables.length];
		for (int i = 0; i < this.inVariables.length; i ++){
			this.inVariables[i] = vars.get(i).getName();
			this.outVariables[i] = vars.get(i).getName();
		}
		this.id = ruleID;
	}
	
	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		Map<String,Node> ret = BindingsUtils.arrayToMap(
									BindingsUtils.mapToArray(
										bindings,
										this.inVariables
									),
									this.outVariables
								);
		
		Context out = new Context();
		out.put("bindings", ret);
		out.put("rule", this.id);
		List<Context> ctxs = new ArrayList<Context>();
		ctxs.add(out);
		return ctxs;
	}

	@Override
	public void setup() { }

	@Override
	public void cleanup() { }

	@Override
	public List<String> variables() {
		List<String> vars = new ArrayList<String>();
		for (String v : this.outVariables)
			vars.add(v);
		return vars;
	}

	@Override
	public String anonimised(Map<String, Integer> varmap) {
		// TODO use chain of "[string]".replaceFirst("%s","[replacement]").replaceFirst(...,...)... instead of String.format("[string]","[replacement]",...)
		return anonimised();
	}

	@Override
	public String anonimised() {
		// TODO
		return "";//VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause);
	}

	@Override
	public void mapVariables(Map<String, String> varmap) {
		String[] newInVars = new String[this.inVariables.length];
		for (int i = 0; i < newInVars.length; i++)
			newInVars[i] = varmap.get(this.inVariables[i]);
	}
	
	@Override
	public String toString() {
		return String.format("%s -> %s", Arrays.toString(this.inVariables), Arrays.toString(this.outVariables));
	}

}
