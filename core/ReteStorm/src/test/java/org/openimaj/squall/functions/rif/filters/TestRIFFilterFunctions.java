package org.openimaj.squall.functions.rif.filters;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TestRIFFilterFunctions {
	
	private RuleWrappedFunction<? extends IFunction<Context, Context>> filter;
	private Context context;
	
	/**
	 * 
	 */
	@Before
	public void before(){
		this.context = new Context();
	}
	
	/**
	 * 
	 */
	@Test
	public void testBaseTripleFilterFunction() {
		TriplePattern tp = new TriplePattern(
				NodeFactory.createVariable("foo"),
				NodeFactory.createURI("http://example.com/p1"),
				NodeFactory.createVariable("bar")
		);
		
		Triple t = new Triple(
				NodeFactory.createLiteral("foo"),
				NodeFactory.createURI("http://example.com/p1"),
				NodeFactory.createLiteral("bar")
		);
		
		this.filter = BaseTripleFilterFunction.ruleWrapped(tp);
		
		this.context.put("triple",t);
	}
	
	/**
	 * 
	 */
	@Test
	public void testBaseAtomFilterFunction() {
		Functor fp = new Functor(
				"p1",
				new Node[]{
						NodeFactory.createVariable("foo"),
						NodeFactory.createVariable("bar")		
				}
		);
		
		Functor f = new Functor(
				"p1",
				new Node[]{
						NodeFactory.createLiteral("foo"),
						NodeFactory.createLiteral("bar")		
				}
		);
		
		this.filter = BaseAtomFilterFunction.ruleWrapped(fp);
		
		this.context.put("atom",f);
	}
	
	/**
	 * 
	 */
	@After
	public void after(){
		List<Context> results = filter.getWrapped().apply(context);
		assertFalse(results == null);
		System.out.println(results);
	}
	
}
