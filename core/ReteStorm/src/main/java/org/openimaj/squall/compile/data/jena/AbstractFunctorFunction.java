package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.squall.compile.data.IVFunction;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T1>
 * @param <T2>
 */
public abstract class AbstractFunctorFunction<T1, T2>  implements IVFunction<T1, T2> {
	protected Functor clause;
	private ArrayList<String> variables;
	protected Rule rule;
	private Node_RuleVariable[] ruleVariables;

	/**
	 * @param r The rule which the functor is part of
	 * @param clause construct using a {@link TriplePattern}
	 */
	public AbstractFunctorFunction(Rule r, Functor clause) {
		this.clause = clause;
		this.rule = r;
		this.ruleVariables = BindingsUtils.extractRuleVariables(r);
		this.variables = new ArrayList<String>();
		for (Node iterable_element : clause.getArgs()) {
			if(iterable_element.isVariable()){
				this.variables.add(iterable_element.getName());
			}
		}
	}

	@Override
	public List<String> variables() {
		return this.variables;
	}
	
	@Override
	public String anonimised(Map<String, Integer> varmap) {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause,varmap);
	}

	@Override
	public String anonimised() {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause);
	}
	
	protected Map<String, Node> bToMap(BindingVector be) {
		
		return BindingsUtils.bindingsToMap(be, ruleVariables);
	}

	protected BindingVector mapToB(Map<String, Node> in) { 
		
		return BindingsUtils.mapToBindings(in, ruleVariables);
	}
	
	@Override
	public void setup() {	}
	
	@Override
	public void cleanup() { }
}
