package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TripleConsequence implements IVFunction<Context,Context> {

	private TriplePattern clause;
	private Node_RuleVariable[] ruleVariables;

	/**
	 * @param r 
	 * @param clause
	 */
	public TripleConsequence(Rule r,TriplePattern clause) {
		this.clause = clause;
		this.ruleVariables = BindingsUtils.extractRuleVariables(r);
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		BindingVector env = BindingsUtils.mapToBindings(bindings, ruleVariables);
		Triple t = env.instantiate(this.clause);
		List<Triple> ret = new ArrayList<Triple>();
		if (!Functor.isFunctor(t.getSubject())) {
			ret.add(t);
		}
		
		List<Context> ctxs = new ArrayList<Context>();
		for (Triple context : ret) {
			Context out = new Context();
			out.put("triple", context);			
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
