package org.openimaj.rdf.rules;

import java.net.URI;

/**
 * Lists the functions required of a class of objects that construct some set of rules a given format.
 * @author David Monks <david.monks@zepler.net>
 */
public interface RuleConstructor {
	
	/**
	 * 
	 */
	public void initialisePrefixes();
	
	/**
	 * @param pref 
	 * @param uri
	 */
	public void addPrefix(String pref, URI uri);
	
	/**
	 * 
	 */
	public void initialiseImports();
	
	/**
	 * @param locator
	 * @param profile
	 */
	public void addImport(URI locator, URI profile);
	
	/**
	 * 
	 */
	public void startRuleSet();
	/**
	 * @param name
	 */
	public void startRuleSet(String name);
	
	/**
	 * 
	 */
	public void startRule();
	/**
	 * @param name
	 */
	public void startRule(String name);
	
	/**
	 * 
	 */
	public void startHead();
	/**
	 * 
	 */
	public void startBody();
	
	/**
	 * 
	 */
	public void startGraph();
	
	/**
	 * @param subject 
	 * @param predicate 
	 * @param object 
	 */
	public void createTriple(String subject, String predicate, String object);
	
	/**
	 * 
	 */
	public void endGraph();
	
	/**
	 * 
	 */
	public void endHead();
	/**
	 * 
	 */
	public void endBody();
	
	/**
	 * 
	 */
	public void endRule();
	/**
	 * @param name
	 */
	public void endRule(String name);
	
	/**
	 * 
	 */
	public void endRuleSet();
	/**
	 * @param name
	 */
	public void endRuleSet(String name);
	
}
