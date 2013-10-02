package org.openimaj.squall.build;

import java.util.Collection;
import java.util.List;

import org.openimaj.squall.orchestrate.ComponentInformationFunctionNode;
import org.openimaj.squall.orchestrate.OrchestratedProductionSystem;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.SplitStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * From an {@link OrchestratedProductionSystem} construct an OpenIMAJ {@link Stream} based 
 * instantiation
 *
 */
public class OIStreamBuilder implements Builder{

	private CollectionStream<Triple> triples;

	public OIStreamBuilder(List<Triple> tripleList) {
		// Start the triple stream
		this.triples = new CollectionStream<Triple>(tripleList);
	}

	@Override
	public void build(OrchestratedProductionSystem ops) {
		SplitStream<Triple> splitStream = new SplitStream<Triple>(this.triples);
		// Get the root's children, these feed directly from the triple stream
		for (ComponentInformationFunctionNode child : ops.root) {
			
		}
	}

}
