package org.openimaj.squall.functions.rif.consequences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.rif.BindingsUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFTripleConsequence implements IVFunction<Context,Context> {

	private TriplePattern clause;
	private Node_RuleVariable[] ruleVariables;

	public RIFTripleConsequence(TriplePattern clause) {
		this.clause = clause;
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		
		List<Triple> ret = new ArrayList<Triple>();
		ret.add(BindingsUtils.instantiate(this.clause,bindings));
		
		List<Context> ctxs = new ArrayList<Context>();
		for (Triple triple : ret) {
			Context out = new Context();
			out.put("triple", triple);
			ctxs.add(out);			
		}
		return ctxs;
	}

	@Override
	public void setup() { }

	@Override
	public void cleanup() { }

	@Override
	public List<String> variables() {
		// TODO
		return new ArrayList<String>();
	}

	@Override
	public String anonimised(Map<String, Integer> varmap) {
		// TODO correct behaviour
		return anonimised();
	}

	@Override
	public String anonimised() {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause);
	}

	@Override
	public void mapVariables(Map<String, String> varmap) {
		// TODO Implement Variable Mapping
		
	}

	@Override
	public String toString() {
		return this.clause.toString();
	}
	
}