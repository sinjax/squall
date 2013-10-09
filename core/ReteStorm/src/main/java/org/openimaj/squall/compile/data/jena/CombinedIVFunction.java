package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class CombinedIVFunction<A,B> implements IVFunction<A,B> {

	private List<IVFunction<A, B>> functions;

	/**
	 * @param functions 
	 * @param r
	 * @param clause
	 */
	public CombinedIVFunction() {
		this.functions = new ArrayList<IVFunction<A,B>>();
	}
	
	public void addFunction(IVFunction<A,B> func){
		this.functions.add(func);
	}
	
	@Override
	public B apply(A in) {
		B out = initial();
		for (IVFunction<A,B> func: this.functions) {
			out = combine(out,func.apply(in));
		}
		return out;
	}

	protected abstract B combine(B out, B apply) ;

	protected abstract B initial() ;
	
	@Override
	public List<String> variables() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String anonimised() {
		String out = "";
		for (IVFunction<A, B> func : this.functions) {
			out += func.anonimised() + " ";
		}
		return out.trim();
	}
	
	@Override
	public String anonimised(Map<String, Integer> varmap) {
		String out = "";
		for (IVFunction<A, B> func : this.functions) {
			out += func.anonimised(varmap) + " ";
		}
		return out;
	}
	
	@Override
	public void setup() {
		for (IVFunction<A, B> func : this.functions) {
			func.setup();
		}
	}
	
	@Override
	public void cleanup() {
		for (IVFunction<A, B> func : this.functions) {
			func.cleanup();
		}
	}

}
