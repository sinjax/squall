package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Given a binding environment, output a binding environment
 *
 */
public class FunctorFunction extends AbstractFunctorFunction<Context,Context> {
	
	/**
	 * @param r
	 * @param clause
	 */
	public FunctorFunction(Rule r, Functor clause) {
		super(r, clause);
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> object = in.getTyped("bindings");
		BindingVector be = mapToB(object);
		RuleContext context = new ReteTopologyRuleContext.IgnoreAdd(this.rule, be);
		clause.evalAsBodyClause(context);
		Context out = new Context();
		out.put("bindings", bToMap(be));
		
		List<Context> ret = new ArrayList<Context>();
		ret.add(out);
		return ret;
	}

	
	
	
}