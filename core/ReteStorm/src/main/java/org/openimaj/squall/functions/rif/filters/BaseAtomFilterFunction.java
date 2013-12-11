package org.openimaj.squall.functions.rif.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.rif.BindingsUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class BaseAtomFilterFunction implements IVFunction<Context, Context> {

	private final static Logger logger = Logger.getLogger(BaseAtomFilterFunction.class);
	private Functor clause;
	private List<String> variables;

	/**
	 * @param clause construct using a {@link TriplePattern}
	 */
	public BaseAtomFilterFunction(Functor clause) {
		this.clause = clause;
		this.variables = enumerateVariables(clause);
	}
	private static List<String> enumerateVariables(Functor clause){
		List<String> variables = new ArrayList<String>();
		for (Node n : clause.getArgs())
			registerVariable(n, variables);
		return variables;
	}

	private static Node registerVariable(Node n, List<String> variables) {
		if(n.isVariable()){
			variables.add(n.getName());
			return Node.ANY ;
		}
		else if(Functor.isFunctor(n)){
			Functor f = (Functor)n.getLiteralValue();
			for (int i = 0; i < f.getArgs().length; i++){
				Node fnode = f.getArgs()[i];
				if (fnode.isVariable())
				{
					variables.add(fnode.getName());
//					f.getArgs()[i] = Node.ANY;
				}
			}
		}
		return n;
	}
	@Override
	public List<Context> apply(Context inc) {
		List<Context> ctxs = new ArrayList<Context>();
		logger.debug(String.format("Context(%s) sent to Filter(%s)" , inc, this.clause));
		Functor in = inc.getTyped("atom");
		
		Map<String,Node> binds = BindingsUtils.extractVars(this.clause, in);
		if (binds == null) return ctxs;
		
		logger.debug(String.format("Match at Filter(%s): %s", this.clause, inc));
		
		// We have a match!
		Context out = new Context();
		out.put("bindings", binds);
		ctxs.add(out);
		return ctxs;
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
	@Override
	public void setup() { }
	@Override
	public void cleanup() { }
	@Override
	public void mapVariables(Map<String, String> varmap) {
		// TODO Implement Variable Mapping
		
	}
	
	@Override
	public String toString() {
		return String.format("FILTER: %s, variables: %s",this.clause,this.variables.toString());
	}

}
