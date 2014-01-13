package org.openimaj.squall.functions.rif.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class BaseRIFPredicateEqualityFunction extends BaseRIFPredicateFunction {
	private static final Logger logger = Logger.getLogger(BaseRIFPredicateFunction.class);
	
	/**
	 * Constructs a new equality-checking predicate function that filters bindings predicated on equality between the
	 * provided variables and any provided constants.  
	 * @param ns -
	 * 		The array of nodes to be compared
	 * @throws RIFPredicateException 
	 */
	public BaseRIFPredicateEqualityFunction(Node[] ns) throws RIFPredicateException{
		super(ns);
		Node val = null;
		for (Node n : ns){
			if (n.isConcrete()){
				if (val == null){
					val = (Node_Concrete) n;
				}else if (!val.sameValueAs(n)){
					throw new RIFPredicateException("RIF translator: All constants compared must be semantically equal.");
				}
			}
		}
	}
	
	@Override
	public List<Context> apply(Context in) {
		logger.debug(String.format("Context(%s) sent to Predicate(eq%s)" , in, Arrays.toString(super.nodes)));
		Map<String,Node> bindings = in.getTyped("bindings");
		
		List<Context> ret = new ArrayList<Context>();
		int i = 0;
		Object match = super.extractBinding(bindings, super.nodes[i]);
		for (i++; i < super.nodes.length; i++){
			if (!match.equals(super.extractBinding(bindings, super.nodes[i]))){
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
		int i = 0;
		anon.append(super.mapNode(varmap, super.nodes[i]));
		for (i++; i < super.nodes.length; i++){
			anon.append(" = ").append(super.mapNode(varmap, super.nodes[i]));;
		}
		return anon.toString();
	}

	@Override
	public String identifier() {
		StringBuilder anon = new StringBuilder();
		if (super.varHolder == null){
			anon.append(super.varHolder.identifier());
		}
		int i = 0;
		anon.append(super.stringifyNode(super.nodes[i]));
		for (i++; i < super.nodes.length; i++){
			anon.append(" = ").append(super.stringifyNode(super.nodes[i]));;
		}
		return anon.toString();
	}

}
