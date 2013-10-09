package org.openimaj.rdf.rules;

import java.net.URI;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.syntax.Element;

/**
 * Converts an ontology specified in RDF (with any expressivity reasonable by Jena models) to a rule set according to some rule constructor.
 * @author David Monks <david.monks@zepler.net>
 */
public class OntologyToRulesCompiler {
	
	/**
	 * 
	 * @param ontologyURI -
	 * 			The URI of the ontology whose TBox is to be converted into a set of rules.
	 * @param baseRulesURI -
	 * 			The URI of the rule set by whose rules to interpret the ontology's TBox.
	 * @param ruleConstructor -
	 * 			The Object that will construct the set of rules produced by the converter.
	 * @return 
	 * 			True if the ontology was successfully converted, false otherwise.
	 */
	public static boolean compile(URI ontologyURI, URI baseRulesURI, RuleConstructor ruleConstructor) throws /*TODO*/Exception {
		//Fetch Ontology and load into Jena Model
		Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
		model.read(ontologyURI.toASCIIString());
		
		//Fetch Rule Set and use to query Jena Model
		String sparql = "nothing";
			//call on Rule Constructor to construct rules as they are determined.
		Query query = QueryFactory.create(sparql);
		QueryExecution execution = QueryExecutionFactory.create(query, model);
		Iterator<Triple> result = execution.execConstructTriples();
		return false;
	}
	
}
