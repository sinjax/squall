package org.openimaj.squall.compile.owl;

import java.util.Iterator;
import java.util.List;

import org.openimaj.rif.RIFRuleSet;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

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
		List<ISource<Stream<Context>>> sources = type.firstObject();
		ContextCPS ret = new ContextCPS();
		for (ISource<Stream<Context>> source : sources) {
			ret.addSource(source);
		}
		
		// Fetch Ontology and load into Jena Model performing the greatest available amount of in memory static reasoning.
		Model ontology = type.secondObject();
		Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, ontology);
		
		// Fetch Rule Set
		RIFRuleSet rules = type.thirdObject();
		// Use Rule Set to query Jena Model
			// TODO: Convert rules to SPARQL query Strings
		String sparql = "nothing";
		Query query = QueryFactory.create(sparql);
		QueryExecution execution = QueryExecutionFactory.create(query, model);
		Iterator<Triple> result = execution.execConstructTriples();
			// TODO: Run each query in turn, producing a separate Compiled Production System from each row of bindings returned
			//		and the original RIFRule whos query produced it.  
		return null;
	}

}
