package org.openimaj.squall.compile.rif;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.openimaj.squall.compile.data.BindingsUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TestRIFAtomFilterFunction {
	
	/**
	 * 
	 */
	@Test
	public void testRIFAtomFilterFunction(){
		
		Node[] params = new Node[1];
		params[0] = NodeFactory.createVariable("place");
		Node[] args = new Node[1];
		args[0] = NodeFactory.createLiteral("world");
		
		Functor pattern = new Functor("hello", params);
		Functor axiom = new Functor ("hello", args);
		
		Map<String, Node> functorBindings = BindingsUtils.extractVars(pattern, axiom);
		System.out.println(functorBindings);
		assertTrue(functorBindings != null);
		
	}

}
