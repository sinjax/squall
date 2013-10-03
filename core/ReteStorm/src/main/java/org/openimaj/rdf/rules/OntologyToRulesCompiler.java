package org.openimaj.rdf.rules;

import java.net.URI;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
		Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF);
		model.read(ontologyURI.toASCIIString());
		
		//Fetch Rule Set and use to query Jena Model
			//call on Rule Constructor to construct rules as they are determined.
		
		return false;
	}
	
}
