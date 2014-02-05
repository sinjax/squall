package org.openimaj.squall.functions.rif.predicates;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.functions.rif.calculators.BaseValueFunction.RuleWrappedValueFunction;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.rif.predicates.BasePredicateFunction.RuleWrappedPredicateFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TestRIFPredicateFunctions {

	private static final class StubAnonRVarHolder extends AnonimisedRuleVariableHolder {

		public StubAnonRVarHolder(List<Node_Variable> vars){
			super();
			Count count = new Count();
			for (Node_Variable var : vars){
				count.inc();
				this.addVariable(Integer.toString(count.getCount()));
				this.putRuleToBaseVarMapEntry(var.getName(), Integer.toString(count.getCount()));
			}
		}
		
		@Override
		public String identifier(Map<String, String> varmap) {
			// TODO Auto-generated method stub
			return "Stub";
		}

		@Override
		public String identifier() {
			// TODO Auto-generated method stub
			return "Stub";
		}
		
	}
	
	private List<Node_Variable> vars;
	private AnonimisedRuleVariableHolder arvh;
	private RuleWrappedPredicateFunction<? extends BasePredicateFunction> func;
	
	/**
	 * 
	 */
	@Before
	public void before(){
		this.vars = new ArrayList<Node_Variable>();
		this.vars.add((Node_Variable) NodeFactory.createVariable("foo"));
		
		this.arvh = new StubAnonRVarHolder(this.vars);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBaseRIFPredicateEqualityFunction(){
		this.func = null;
		try {
			this.func = PredicateEqualityFunction.ruleWrapped(new Node[]{
					NodeFactory.createLiteral("a"),
					NodeFactory.createLiteral("a")
			}, new HashMap<Node, RuleWrappedValueFunction<?>>());
		} catch (RIFPredicateException e) {
			System.out.println(e.getMessage());
		}
		assertTrue(this.func == null);
		
		try {
			this.func = PredicateEqualityFunction.ruleWrapped(new Node[]{
					NodeFactory.createLiteral("a"),
					this.vars.get(0)
			}, new HashMap<Node, RuleWrappedValueFunction<?>>());
		} catch (RIFPredicateException e) {
			System.out.println(e.getMessage());
		}
		assertFalse(this.func == null);
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testLiteralNotEqualFunction(){
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testNumericGreaterThanFunction(){
		
	}
	
}
