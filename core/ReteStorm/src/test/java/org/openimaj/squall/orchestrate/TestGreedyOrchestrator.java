package org.openimaj.squall.orchestrate;

import java.util.List;

import org.junit.Before;
import org.openimaj.squall.compile.jena.TestJenaRuleCompiler;
import org.openimaj.squall.utils.JenaUtils;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestGreedyOrchestrator {
	
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
	public void testJenaRules(){
	}
}
