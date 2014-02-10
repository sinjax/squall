package org.openimaj.squall.functions.rif.consequences;

import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.InheritsVariables;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.functions.consequences.AtomConsequence;
import org.openimaj.squall.functions.consequences.BaseBindingConsequence;
import org.openimaj.squall.functions.consequences.TripleConsequence;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TestRIFConsequenceFunctions {

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
	private RuleWrappedFunction<? extends IFunction<Context, Context>> func;
	
	/**
	 * 
	 */
	@Before
	public void before(){
		this.vars = new ArrayList<Node_Variable>();
		this.vars.add((Node_Variable) NodeFactory.createVariable("foo"));
		this.vars.add((Node_Variable) NodeFactory.createVariable("bar"));
		
		this.arvh = new StubAnonRVarHolder(vars);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBaseBindingConsequence(){
		this.func = BaseBindingConsequence.ruleWrapped(vars, "Fake_Rule");
	}
	
	/**
	 * 
	 */
	@Test
	public void testRIFTripleConsequence(){
		TriplePattern tp = new TriplePattern(
				NodeFactory.createVariable("foo"),
				NodeFactory.createURI("http://example.org/p1"),
				NodeFactory.createVariable("bar")
		);
		
		this.func = TripleConsequence.ruleWrapped(tp, "Fake_Rule");
	}
	
	/**
	 * 
	 */
	@Test
	public void testRIFAtomConsequence(){
		Functor f = new Functor(
				"p1",
				new Node[]{
						NodeFactory.createVariable("foo"),
						NodeFactory.createVariable("bar")
				}
		);
		
		this.func = AtomConsequence.ruleWrapped(f, "Fake_Rule");
	}
	
	/**
	 * 
	 */
	@After
	public void after(){
		((InheritsVariables) func).setSourceVariables(arvh);
		
		Context cont = new Context();
		Map<String, Node> binds = new HashMap<String, Node>();
		binds.put("1", NodeFactory.createLiteral("foo"));
		binds.put("2", NodeFactory.createLiteral("bar"));
		cont.put("bindings", binds);
		
		List<Context> results = this.func.getWrapped().apply(cont);
		assertFalse(results == null);
		System.out.println(results);
	}
	
}
