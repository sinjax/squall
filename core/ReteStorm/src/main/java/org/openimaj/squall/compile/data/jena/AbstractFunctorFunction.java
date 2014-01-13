package org.openimaj.squall.compile.data.jena;

import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T1>
 * @param <T2>
 */
public abstract class AbstractFunctorFunction<T1, T2> extends IVFunction<T1, T2> {
	
	protected Functor clause;
	private Node_RuleVariable[] ruleVariables;

	/**
	 * @param r The rule which the functor is part of
	 * @param clause construct using a {@link TriplePattern}
	 */
	public AbstractFunctorFunction(Rule r, Functor clause) {
		super();
		this.clause = clause;
		this.ruleVariables = BindingsUtils.extractRuleVariables(r);
		for (Node iterable_element : clause.getArgs()) {
			if(iterable_element.isVariable()){
				this.addVariable(iterable_element.getName());
				this.putRuleToBaseVarMapEntry(iterable_element.getName(), iterable_element.getName());
			}
		}
	}
	
	private String mapNode(Map<String,String> varmap, Node node){
		String nodeString = this.stringifyNode(node);
		String mappedString;
		return node.isVariable()
				? (mappedString = varmap.get(nodeString)) == null
					? "VAR"
					: mappedString
				: nodeString;
	}
	@Override
	public String identifier(Map<String, String> varmap) {
		StringBuilder obj = new StringBuilder();
		obj.append(this.clause.getName()).append("(")
		   .append(this.mapNode(varmap, this.clause.getArgs()[0]));
		for (int i = 1; i < this.clause.getArgLength(); i++){
			obj.append(",").append(this.mapNode(varmap, this.clause.getArgs()[i]));
		}
		obj.append(")");
		return obj.toString();
	}
	
	private String stringifyNode(Node node){
		return node.isVariable() ? "?"+this.indexOfVar(node.getName()) : node.toString();
	}
	@Override
	public String identifier() {
		StringBuilder obj = new StringBuilder();
		obj.append(this.clause.getName()).append("(")
		   .append(this.stringifyNode(this.clause.getArgs()[0]));
		for (int i = 1; i < this.clause.getArgLength(); i++){
			obj.append(",").append(this.stringifyNode(this.clause.getArgs()[i]));
		}
		obj.append(")");
		return obj.toString();
	}
	
	protected Map<String, Node> bToMap(BindingVector be) {
		return BindingsUtils.bindingsToMap(be, ruleVariables);
	}

	protected BindingVector mapToB(Map<String, Node> in) { 
		return BindingsUtils.mapToBindings(in, ruleVariables);
	}
	
	@Override
	public void setup() {	}
	
	@Override
	public void cleanup() { }
	
}
