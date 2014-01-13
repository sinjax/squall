package org.openimaj.squall.functions.rif.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.rif.AbstractRIFFunction;
import org.openimaj.squall.compile.data.rif.BindingsUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class BaseTripleFilterFunction extends AbstractRIFFunction {

	private final static Logger logger = Logger.getLogger(BaseTripleFilterFunction.class);
	private TriplePattern clause;

	/**
	 * @param clause construct using a {@link TriplePattern}
	 */
	public BaseTripleFilterFunction(TriplePattern clause) {
		super();
		Count count = new Count();
		this.clause = new TriplePattern(
			registerVariable(clause.getSubject(), count),
			registerVariable(clause.getPredicate(), count),
			registerVariable(clause.getObject(), count)
		);
	}

	@Override
	protected Node registerVariable(Node n, Count count) {
		n = super.registerVariable(n, count);
		if(Functor.isFunctor(n)){
			Functor f = (Functor)n.getLiteralValue();
			Node[] newArgs = new Node[f.getArgLength()];
			for (int i = 0; i < f.getArgs().length; i++){
				newArgs[i] = super.registerVariable(f.getArgs()[i], count);
			}
			return Functor.makeFunctorNode(f.getName(), newArgs);
		}
		return n;
	}
	
	@Override
	public List<Context> apply(Context inc) {
		List<Context> ctxs = new ArrayList<Context>();
		Triple in = inc.getTyped("triple");
		
		Map<String,Node> binds = BindingsUtils.extractVars(this.clause, in);
//		logger.debug(String.format("Testing at Filter(%s): %s", this.clause, inc));
		if (binds == null) return ctxs;
		logger.debug(String.format("Match at Filter(%s): %s", this.clause, inc));
		
		// We have a match!
		Context out = new Context();
		out.put("bindings", binds);
		ctxs.add(out);
		return ctxs ;
	}

	@Override
	public String identifier() {
		String subject = super.stringifyNode(this.clause.getSubject()),
			   predicate = super.stringifyNode(this.clause.getPredicate()),
			   object;
		if (Functor.isFunctor(this.clause.getObject())){
			StringBuilder obj = new StringBuilder();
			Functor f = (Functor) this.clause.getObject().getLiteralValue();
			obj.append(f.getName()).append("(")
			   .append(super.stringifyNode(f.getArgs()[0]));
			for (int i = 1; i < f.getArgLength(); i++){
				obj.append(",").append(super.stringifyNode(f.getArgs()[i]));
			}
			obj.append(")");
			object = obj.toString();
		}else{
			object = super.stringifyNode(this.clause.getObject());
		}
		
		StringBuilder name = new StringBuilder("(");
		name.append(subject).append(" ")
			.append(predicate).append(" ")
			.append(object).append(")");
		return name.toString();
	}
	
	@Override
	public String identifier(Map<String, String> varmap) {
		String subject = super.mapNode(varmap, this.clause.getSubject()),
			   predicate = super.mapNode(varmap, this.clause.getPredicate()),
			   object;
		if (Functor.isFunctor(this.clause.getObject())){
			StringBuilder obj = new StringBuilder();
			Functor f = (Functor) this.clause.getObject().getLiteralValue();
			obj.append(f.getName()).append("(")
			   .append(super.mapNode(varmap, f.getArgs()[0]));
			for (int i = 1; i < f.getArgLength(); i++){
				obj.append(",").append(super.mapNode(varmap, f.getArgs()[i]));
			}
			obj.append(")");
			object = obj.toString();
		}else{
			object = super.mapNode(varmap, this.clause.getObject());
		}
		
		StringBuilder name = new StringBuilder("(");
		name.append(subject).append(" ")
			.append(predicate).append(" ")
			.append(object).append(")");
		return name.toString();
	}
	
	@Override
	public void setup() { }
	@Override
	public void cleanup() { }
	
	@Override
	public String toString() {
		return String.format("FILTER: %s, variables: %s",this.clause,this.variables().toString());
	}

}
