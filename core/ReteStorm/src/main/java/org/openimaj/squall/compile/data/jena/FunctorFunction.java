package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;
import org.openimaj.squall.functions.calculators.BaseValueFunction;
import org.openimaj.squall.functions.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.predicates.BasePredicateFunction;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks (dm11g08@ecs.soton.ac.uk)
 * Given a binding environment, output a binding environment
 *
 */
public class FunctorFunction extends BasePredicateFunction {
	
	/**
	 * @param r
	 * @param clause
	 * @return
	 * @throws RIFPredicateException
	 */
	public static RuleWrappedFunctorFunction ruleWrapped(Rule r, Functor clause) throws RIFPredicateException{
		return new RuleWrappedFunctorFunction(r, clause);
	}
	
	private Rule rule;
	private Functor clause;
	private Node_RuleVariable[] ruleVariables;
	
	/**
	 * @param r
	 * @param clause
	 * @throws RIFPredicateException 
	 */
	public FunctorFunction(Rule r, Functor clause) throws RIFPredicateException {
		super(clause.getArgs().clone(), new HashMap<Node, BaseValueFunction>());
		if (clause != null){
			this.clause = clause;
		}
		if (r != null){
			this.ruleVariables = BindingsUtils.extractRuleVariables(r);
		}
		this.rule = r;
	}
		
	@Override
	public BasePredicateFunction clone() {
		try {
			return new FunctorFunction(this.rule, this.clause);
		} catch (RIFPredicateException e) {
			throw new RuntimeException("Clone of valid function should not be invalid.", e);
		}
	}

	protected Map<String, Node> bToMap(BindingVector be) {
		return BindingsUtils.bindingsToMap(be, ruleVariables);
	}

	protected BindingVector mapToB(Map<String, Node> in) {
		
		return BindingsUtils.mapToBindings(in, this.ruleVariables);
	}
	
	@Override
	protected List<Context> applyRoot(Context in) {
		List<Context> ret = new ArrayList<Context>();
		Map<String,Node> object = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		BindingVector be = mapToB(object);
		RuleContext context = new ReteTopologyRuleContext.IgnoreAdd(this.rule, be);
		if(!clause.evalAsBodyClause(context)) return ret;
		
		ret.add(new Context(ContextKey.BINDINGS_KEY.toString(), bToMap(be)));
		return ret;
	}
	
	@Override
	public int mapNodeVarNames(Map<String, String> directVarMap) {
		int ret = super.mapNodeVarNames(directVarMap);
		for (int i = 0; i < this.ruleVariables.length; i++) {
			this.ruleVariables[i] = new Node_RuleVariable(directVarMap.get(this.ruleVariables[i].getName()), i);
		}
		Node[] newArgs = new Node[this.clause.getArgLength()];
		for (int i = 0; i < newArgs.length; i++) {
			if (this.clause.getArgs()[i].isVariable()){
				newArgs[i] = new Node_RuleVariable(directVarMap.get(this.clause.getArgs()[i].getName()),
						((Node_RuleVariable)this.clause.getArgs()[i]).getIndex());
			} else {
				newArgs[i] = this.clause.getArgs()[i];
			}
		}
		this.clause = new Functor(this.clause.getName(), newArgs);
		return ret;
	}

	// required for deserialisation by reflection
	private FunctorFunction() throws RIFPredicateException{
		super(new Node[]{
				NodeFactory.createVariable("foo"),
				NodeFactory.createLiteral("bar")
		}, new HashMap<Node, BaseValueFunction>());
	}

	@Override
	public void write(Kryo kryo, Output output) {
		super.write(kryo, output);
		kryo.writeClassAndObject(output, this.rule);
		kryo.writeClassAndObject(output, this.clause);
		kryo.writeClassAndObject(output, this.ruleVariables);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		super.read(kryo, input);
		this.rule = (Rule) kryo.readClassAndObject(input);
		this.clause = (Functor) kryo.readClassAndObject(input);
		this.ruleVariables = (Node_RuleVariable[]) kryo.readClassAndObject(input);
	}
	
	protected static class RuleWrappedFunctorFunction extends RuleWrappedPredicateFunction<FunctorFunction> {

		protected RuleWrappedFunctorFunction(Rule r, Functor clause) throws RIFPredicateException {
			super(clause.getName(), clause.getArgs(), new HashMap<Node, RuleWrappedValueFunction<?>>());
			for (Node n : clause.getArgs()){
				if (n.isVariable()){
					this.addVariable(n.getName());
					this.putRuleToBaseVarMapEntry(n.getName(), n.getName());
				}
			}
			this.wrap(new FunctorFunction(r, clause));
		}
		
	}
	
}