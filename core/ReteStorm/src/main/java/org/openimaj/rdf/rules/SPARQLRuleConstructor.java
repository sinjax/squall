package org.openimaj.rdf.rules;

import java.net.URI;

public class SPARQLRuleConstructor implements RuleConstructor {

	private final StringBuilder output;
	private StringBuilder prefixes;
	private StringBuilder imports;
	
	/**
	 * 
	 */
	public SPARQLRuleConstructor (){
		output = new StringBuilder();
	}
	
	/**
	 * @return
	 * 			The String of the queries created so far.
	 */
	public String getQuerySets(){
		return output.toString();
	}
	
	@Override
	public void initialisePrefixes() {
		prefixes = new StringBuilder();
	}
	
	@Override
	public void addPrefix(String pref, URI uri) {
		if (prefixes == null) initialisePrefixes();
		
		prefixes.append("PREFIX ");
		prefixes.append(pref);
		prefixes.append(": <");
		prefixes.append(uri.toASCIIString());
		prefixes.append(">\n");
	}
	
	protected String getPrefixes(){
		return prefixes.toString();
	}
	
	@Override
	public void initialiseImports() {
		imports = new StringBuilder();
	}

	@Override
	public void addImport(URI locator, URI profile) {
		if (imports == null) initialiseImports();
		// TODO Auto-generated method stub
	}
	
	protected String getImports(){
		return imports.toString();
	}
	
	@Override
	public void startRuleSet() {
		startRuleSet("no name");
	}

	@Override
	public void startRuleSet(String name) {}

	@Override
	public void startRule() {
		startRule("no name");
	}

	@Override
	public void startRule(String name) {}

	@Override
	public void startHead() {
		output.append("CONSTRUCT\n");
	}

	@Override
	public void startBody() {
		output.append("WHERE\n");
	}

	@Override
	public void startGraph() {
		output.append("{\n");
	}

	@Override
	public void createTriple(String subject, String predicate, String object) {
		output.append(subject);
		output.append(" ");
		output.append(predicate);
		output.append(" ");
		output.append(object);
		output.append(" .\n");
	}

	@Override
	public void endGraph() {
		output.append("}");
	}

	@Override
	public void endHead() {
		output.append("\n");
	}

	@Override
	public void endBody() {
		output.append("\n");
	}

	@Override
	public void endRule() {
		endRule("no name");
	}

	@Override
	public void endRule(String name) {
		output.append("\n");
	}

	@Override
	public void endRuleSet() {
		endRuleSet("no name");
	}

	@Override
	public void endRuleSet(String name) {
		output.insert(0, getPrefixes());
	}

}
