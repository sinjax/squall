package org.openimaj.rdf.storm.sparql.topology.builder.datasets;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.openjena.riot.RiotReader;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class InMemoryDataset extends StaticRDFDatasetBase{

	/**
	 *
	 */
	private static final long serialVersionUID = -3624947878522933907L;
	private String dataset;
	private Model model;

	/**
	 * @param dataset
	 */
	public InMemoryDataset(String dataset){
		this.dataset = dataset;
	}

	@Override
	public void prepare() {
		this.model = RDFDataMgr.loadModel(dataset,Lang.NTRIPLES);
	}

	@Override
	public QueryExecution createExecution(Query query) {
		return QueryExecutionFactory.create(query, model);
	}

	@Override
	public QueryExecution createExecution(Query query,QuerySolution sol) {
		return QueryExecutionFactory.create(query, model,sol);
	}

}
