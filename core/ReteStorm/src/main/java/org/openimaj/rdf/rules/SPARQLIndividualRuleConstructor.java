package org.openimaj.rdf.rules;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPARQLIndividualRuleConstructor extends SPARQLRuleConstructor {

	private final Map<String,Map<String,StringBuilder>> querySets;
	
	private Map<String,StringBuilder> currentSet;
	private String currentSetName;
	
	private StringBuilder output;
	private String outputName;
	
	private int setCount;
	private int queryCount;
	
	/**
	 * 
	 */
	public SPARQLIndividualRuleConstructor (){
		querySets = new HashMap<String,Map<String,StringBuilder>>();
		setCount = 0;
	}
	
	/**
	 * @return
	 * 			The String of the queries created so far.
	 */
	public String getQuerySets(){
		StringBuilder queries = new StringBuilder();
		for (String setKey : querySets.keySet()){
			if (setKey.equals(currentSetName)) break;
			queries.append("Query Set: ");
			queries.append(setKey);
			queries.append("\n---------------------\n");
			for (String queryKey : querySets.get(setKey).keySet()){
				if (queryKey.equals(outputName)) break;
				queries.append("Query: ");
				queries.append(queryKey);
				queries.append("\n");
				queries.append(querySets.get(setKey).get(queryKey).toString());
			}
		}
		return queries.toString();
	}
	
	/**
	 * @return
	 * 			The List of all queries created so far.
	 */
	public List<String> getIndividualQueries(){
		List<String> queries = new ArrayList<String>();
		
		for (String setKey : querySets.keySet()){
			if (setKey.equals(currentSetName)) break;
			for (String queryKey : querySets.get(setKey).keySet()){
				if (queryKey.equals(outputName)) break;
				queries.add(querySets.get(setKey).get(queryKey).toString());
			}
		}
		
		return queries;
	}
	
	@Override
	public void startRuleSet() {
		startRuleSet("no name");
	}

	@Override
	public void startRuleSet(String name) {
		currentSet = new HashMap<String,StringBuilder>();
		queryCount = 0;
		
		currentSetName = Integer.toString(++setCount)+" - "+name;
		querySets.put(currentSetName, currentSet);
	}
	

	@Override
	public void startRule() {
		startRule("no name");
	}

	@Override
	public void startRule(String name) {
		output = new StringBuilder();
		output.append(getPrefixes());
		
		outputName = Integer.toString(++queryCount)+" - "+name;
		currentSet.put(outputName, output);
	}
	
	@Override
	public void endRule() {
		endRule("no name");
	}

	@Override
	public void endRule(String name) {
		output = null;
		outputName = null;
	}
	
	@Override
	public void endRuleSet() {
		endRuleSet("no name");
	}

	@Override
	public void endRuleSet(String name) {
		currentSet = null;
		currentSetName = null;
	}
	
}
