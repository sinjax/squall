package org.openimaj.squall.compile.jena;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.squall.utils.JenaUtils;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestJenaRuleCompiler {
	
	List<Rule> rules;
	
	/**
	 * 
	 */
	@Before
	public void before(){
		this.rules = JenaUtils.readRules(TestJenaRuleCompiler.class.getResourceAsStream("/test.rules"));
		
	}
	
	/**
	 * 
	 */
	@Test
	public void testCompiler(){
		JenaRuleCompiler jrc = new JenaRuleCompiler();
		jrc.compile(rules);
	}

}
