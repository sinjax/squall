package org.openimaj.squall.functions.rif.consequences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.rif.BindingsUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFAtomConsequence implements IVFunction<Context, Context> {
	
	private static final Logger logger = Logger.getLogger(RIFAtomConsequence.class);
	private Functor clause;
	private String id;

	/**
	 * @param tp
	 * @param ruleID
	 */
	public RIFAtomConsequence(Functor tp, String ruleID) {
		this.clause = tp;
		id = ruleID;
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		
		List<Functor> ret = new ArrayList<Functor>();
		ret.add(BindingsUtils.instantiate(this.clause,bindings));
		
		List<Context> ctxs = new ArrayList<Context>();
		for (Functor atom : ret) {
			logger.debug(String.format("completed: [%s] -> %s",this.toString(), bindings));
			
			Context out = new Context();
			out.put("atom", atom);
			out.put("rule", this.id);
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
		return String.format("CONSEQUENCE: clause %s",this.clause.toString());
	}

}
