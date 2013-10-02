package org.openimaj.squall.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Some helper functions for jena
 *
 */
public class JenaUtils {
	
	/**
	 * @param stream
	 * @return rules found in a stream
	 */
	public static List<Rule> readRules(InputStream stream){
		List<Rule> rules = Rule.parseRules(Rule.rulesParserFromReader(new BufferedReader(new InputStreamReader(stream))));
		return rules;
	}
}
