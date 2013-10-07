package org.openimaj.squall.compile.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.util.PrintUtil;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Given a binding environment, output a binding environment
 *
 */
public class FunctorFunction implements VariableFunction<Map<String, String>, Map<String, String>> {
	private Functor clause;

	/**
	 * @param clause construct using a {@link TriplePattern}
	 */
	public FunctorFunction(Functor clause) {
		this.clause = clause;
	}

	@Override
	public List<String> variables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> apply(Map<String, String> in) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String anonimised(Map<String, Integer> varmap) {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause,varmap);
	}

	@Override
	public String anonimised() {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause);
	}
	
	
}