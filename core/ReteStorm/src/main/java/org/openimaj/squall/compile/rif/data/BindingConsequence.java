package org.openimaj.squall.compile.rif.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.jena.BindingsUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author david.monks
 *
 */
public class BindingConsequence implements IVFunction<Context,Context> {

	private Node_RuleVariable[] inVariables;
	private Node_RuleVariable[] outVariables;
	
	/**
	 * @param rule
	 */
	public BindingConsequence(Rule rule){
		this.inVariables = BindingsUtils.extractRuleVariables(rule.getBody()).toArray(this.inVariables);
		this.outVariables = BindingsUtils.extractRuleVariables(rule.getHead()).toArray(this.outVariables);
	}
	
	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		Map<String,Node> ret = BindingsUtils.bindingsToMap(
																		BindingsUtils.mapToBindings(
																										bindings,
																										this.inVariables
																									),
																		this.outVariables
																	);
		
		Context out = new Context();
		out.put("bindings", ret);
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
		for (Node_RuleVariable n : this.outVariables)
			vars.add(n.getName());
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
		// TODO Implement Variable Mapping
		
	}

}
