package org.openimaj.squall.functions.rif.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.rif.data.BindingsUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Concrete;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class BaseRIFPredicateEqualityFunction extends BaseRIFPredicateFunction {
	
	private Node_Concrete val;
	
	/**
	 * Constructs a new equality-checking predicate function that filters bindings predicated on equality between the
	 * provided variables and any provided constants.  
	 * @param ns -
	 * 		The array of nodes to be compared
	 * @throws RIFPredicateException 
	 */
	public BaseRIFPredicateEqualityFunction(Node[] ns) throws RIFPredicateException{
		super(ns);
		this.val = null;
		for (Node n : ns){
			if (!n.isVariable())
				if (this.val == null)
					this.val = (Node_Concrete) n;
				else if (!this.val.sameValueAs(n))
					throw new RIFPredicateException("RIF translator: All constants compared must be semantically equal.");
		}
	}
	
	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		Node[] env = BindingsUtils.mapToArray(bindings,vars);
		
		try{
			if (!val.sameValueAs(env[0])) return null;
		} catch (NullPointerException e) {}
		
		for (int j = 1; j < env.length; j++){
			if (!env[0].sameValueAs(env[j])) return null;
		}
		
		List<Context> ret = new ArrayList<Context>();
		ret.add(in);
		return ret;
	}

	@Override
	public String anonimised(Map<String, Integer> varMap) {
		StringBuilder anon = new StringBuilder();
		if (this.val != null){
			anon.append(val.isURI()
							? val.getURI()
							: val.isLiteral()
								? val.getLiteralLexicalForm()
								: val.getBlankNodeLabel());
			anon.append(" = ");
		}
		anon.append("?");
		anon.append(varMap.get(this.vars[0]));
		for (int i = 1; i < this.vars.length; i++){
			anon.append(" = ?");
			anon.append(varMap.get(this.vars[i]));
		}
		return anon.toString();
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
