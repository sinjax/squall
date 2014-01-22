package org.openimaj.squall.functions.rif.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class LiteralNotEqualFunction extends BaseRIFPredicateFunction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2256444262394116745L;
	private static final Logger logger = Logger.getLogger(LiteralNotEqualFunction.class);
	
	/**
	 * @param ns
	 * @throws RIFPredicateException
	 */
	public LiteralNotEqualFunction(Node[] ns) throws RIFPredicateException {
		super(ns);
		Node val = null;
		for (Node n : ns){
			if (n.isConcrete()){
				if (val == null){
					val = (Node_Concrete) n;
				}else if (val.sameValueAs(n)){
					throw new RIFPredicateException("RIF translator: All constants compared must be semantically different.");
				}
			}
		}
		if (this.varCount() == 0){
			throw new RIFPredicateException("RIF translator: Predicate must compare at least one variable.");
		}
	}
	
	@Override
	public List<Context> apply(Context in){
		logger .debug(String.format("Context(%s) sent to Predicate(neq(%s))" , in, Arrays.toString(super.nodes)));
		Map<String,Node> binds = in.getTyped("bindings");
		
		List<Context> ret = new ArrayList<Context>();
		int i = 0;
		Object match = super.extractBinding(binds, super.nodes[i]);
		for (i++; i < super.nodes.length; i++){
			if (match.equals(super.extractBinding(binds, super.nodes[i]))){
				return ret;
			}
		}
		ret.add(in);
		
		logger.debug(String.format("Context(%s) passed Predicate(eq%s)" , in, Arrays.toString(super.nodes)));
		return ret;
	}

	@Override
	public String identifier(Map<String, String> varmap) {
		StringBuilder anon = new StringBuilder();
		if (super.varHolder == null){
			anon.append(super.varHolder.identifier(varmap));
		}
		anon.append("LiteralNotEqual(");
		if (super.nodes.length > 0){
			int i = 0;
			anon.append(super.mapNode(varmap, super.nodes[i]));
			for (i++; i < super.nodes.length; i++){
				anon.append(",").append(super.mapNode(varmap, super.nodes[i]));;
			}
		}
		return anon.append(")").toString();
	}

	@Override
	public String identifier() {
		StringBuilder anon = new StringBuilder();
		if (super.varHolder == null){
			anon.append(super.varHolder.identifier());
		}
		anon.append("LiteralNotEqual(");
		if (super.nodes.length > 0){
			int i = 0;
			anon.append(super.stringifyNode(super.nodes[i]));
			for (i++; i < super.nodes.length; i++){
				anon.append(",").append(super.stringifyNode(super.nodes[i]));;
			}
		}
		return anon.append(")").toString();
	}
	
}
