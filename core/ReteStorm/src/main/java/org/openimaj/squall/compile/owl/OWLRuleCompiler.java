package org.openimaj.squall.compile.owl;

import java.util.Iterator;
import java.util.List;

import org.openimaj.rif.RIFRuleSet;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.jena.ContextCPS;
import org.openimaj.squall.compile.jena.IStream;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class OWLRuleCompiler implements Compiler<SourceModelRulesetTrio> {

	@Override
	public CompiledProductionSystem compile(SourceModelRulesetTrio type) {
		// Get Sources and create Context-based Compiled Production system, then add the former to the latter.
		List<IStream<Context>> sources = sourceRules.firstObject();
		ContextCPS ret = new ContextCPS();
		for (IStream<Context> stream : sources) {
			ret.addSource(stream);
		}
		
		// Fetch Ontology and load into Jena Model
//		Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
//		model.read(ontologyURI.toASCIIString());
		Model ontology = type.secondObject();
		Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, ontology);
		
		// Fetch Rule Set
		RIFRuleSet rules = type.thirdObject();
		// Use Rule Set to query Jena Model
		String sparql = "nothing";
			//call on Rule Constructor to construct rules as they are determined.
		Query query = QueryFactory.create(sparql);
		QueryExecution execution = QueryExecutionFactory.create(query, model);
		Iterator<Triple> result = execution.execConstructTriples();
		return null;
	}

}
