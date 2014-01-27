package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IConsequence;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TripleConsequence extends AbstractTripleFunction implements IConsequence {

	/**
	 * @param r 
	 * @param clause
	 */
	public TripleConsequence(Rule r,TriplePattern clause) {
		super(r, clause);
	}
	
	private TripleConsequence(){
		super(null, null);
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
	public String toString() {
		return this.clause.toString();
	}

	@Override
	public void setSourceVariableHolder(AnonimisedRuleVariableHolder arvh) {}

	@Override
	public boolean isReentrant() {
		return true;
	}

}
