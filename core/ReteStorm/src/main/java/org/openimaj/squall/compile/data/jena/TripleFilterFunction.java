package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Filter a triple, return bindings against variables
 *
 */
public class TripleFilterFunction implements IVFunction<Context, Context> {
	private final static Logger logger = Logger.getLogger(TripleFilterFunction.class);
	private TriplePattern clause;
	private Triple extended;
	private List<String> variables;

	/**
	 * 
	 */
	public TripleFilterFunction() {
	}
	/**
	 * @param clause construct using a {@link TriplePattern}
	 */
	public TripleFilterFunction(TriplePattern clause) {
		this.clause = clause;
		this.extended = asExtendedTripleMatch(clause).asTriple();
	}
	private TripleMatch asExtendedTripleMatch(TriplePattern tp){
		this.variables = new ArrayList<String>();
		Triple created = new Triple(
			registerVariable(tp.getSubject()),
			registerVariable(tp.getPredicate()),
			registerVariable(tp.getObject())
		);
		return created;
	}

	private Node registerVariable(Node n) {
		if(n.isVariable()){
			this.variables.add(n.getName());
			return Node.ANY ;
		}
		else if(Functor.isFunctor(n)){
			Functor f = (Functor)n.getLiteralValue();
			for (int i = 0; i < f.getArgs().length; i++){
				Node fnode = f.getArgs()[i];
				if (fnode.isVariable())
				{
					this.variables.add(fnode.getName());
				}
			}
		}
		return n;
	}
	@Override
	public List<Context> apply(Context inc) {
		List<Context> ctxs = new ArrayList<Context>();
		logger.debug(String.format("Context(%s) sent to Filter(%s)" , inc, this.clause));
		Triple in = inc.getTyped("triple");
//		if(!in.getSubject().matches(this.extended.getMatchSubject())) return null;
//		if(!in.getPredicate().matches(this.extended.getMatchPredicate())) return null;
//		if(!in.getObject().matches(this.extended.getMatchObject())) return null;
		if(!this.extended.matches(in)){
			logger.debug(String.format("No match!"));
			return null;
		}
		
		logger.debug(String.format("Match at Filter(%s)", this.clause));
		
		// We have a match!
		Context out = new Context();
		out.put("bindings", extractVars(in));
		ctxs.add(out);
		return ctxs ;
	}

	private Map<String, Node> extractVars(Triple t) {
		TriplePattern filter = clause;
		// Create a Map of Variable Strings to Nodes 
		Map<String,Node> vars = new HashMap<String,Node>();

		// For each part of the triple, check if the Pattern declares it to be variable
		// (or a functor, in the case of Objects)
		if (filter.getSubject().isVariable()){
			// if it is a variable, insert its value into the array of Values
			vars.put(filter.getSubject().getName(),t.getSubject());
		}

		if (filter.getPredicate().isVariable())
			// For each subsequent variable, check that the variable has not already been
			// seen within this triple.
			if (vars.containsKey(filter.getPredicate().getName())){
				// If it has and the values are different, then the Triple is not a match, so
				// do not fire, and move onto the next Triple.
				if ( ! t.getPredicate().sameValueAs( vars.get(filter.getPredicate().getName()) ));
				{
					return null;
				}
			} else {
				// If the variable has not been seen before, process the node as with the Subject.
				vars.put(filter.getPredicate().getName(),t.getPredicate());
			}

		if (filter.getObject().isVariable())
			if (vars.containsKey(filter.getObject().getName())) {
				if ( ! t.getObject().sameValueAs( vars.get( filter.getObject().getName() ) )  )
				{
					return null;
				}
			} else {
				vars.put(filter.getObject().getName(),t.getObject());
			}
		else if (filter.getObject().isLiteral() && filter.getObject().getLiteralValue() instanceof Functor){
			// if the object is a functor, check each node in the functor to see if it is a variable,
			// and treat each as if it were a more traditional part of the Triple.
			Functor f = (Functor)filter.getObject().getLiteralValue();
			Functor functor;
			if (t.getObject().isLiteral()
					&& t.getObject().getLiteralValue() instanceof Functor
					&& (functor = (Functor)t.getObject().getLiteralValue()).getArgLength() == f.getArgLength()) {
				for (int i = 0; i < f.getArgs().length; i++){
					Node n = f.getArgs()[i];
					if (n.isVariable())
						if (vars.containsKey(n.getName())){
							if ( ! functor.getArgs()[i].sameValueAs( vars.get( n.getName() ) ) )
							{
								return null;
							}
						} else {
							vars.put(n.getName(), functor.getArgs()[i]);
						}
					else
					{
						if ( ! n.sameValueAs(functor.getArgs()[i]))
						{
							return null;
						}
					}
				}
			} else
			{
				return null;
			}
		}
		return vars;
	}
	@Override
	public List<String> variables() {
		return this.variables;
	}
	
	public void mapVars(Map<String,String> a){
//		this.variables == alter
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
}