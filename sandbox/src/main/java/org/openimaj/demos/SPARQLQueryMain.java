package org.openimaj.demos;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openimaj.rdf.storm.sparql.topology.StormSPARQLReteTopologyOrchestrator;
import org.openimaj.rdf.storm.sparql.topology.builder.group.StaticDataFileNTriplesSPARQLReteTopologyBuilder;
import org.openimaj.rdf.storm.utils.JenaStormUtils;

import eu.larkc.csparql.streams.formats.TranslationException;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.Utils;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SPARQLQueryMain {
	
	/**
	 * The outputs of this program go directly to a log file in sandbox/logs_retestorm.log
	 * @param args
	 * @throws TranslationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws TranslationException, IOException {
		// The query and the output from the args
		// Try using the explicit location on your machine of the query.csparql in the ReteStormTool examples directory
		File query = new File(args[0]);
		File output = new File(args[1]);
		
		// The builder needs only know where to write the output. It doesn't ask for the input. 
		// This is defined explicitly in the query 
		StaticDataFileNTriplesSPARQLReteTopologyBuilder builder = new StaticDataFileNTriplesSPARQLReteTopologyBuilder(output);
		
		// Storm uses kryo to serialise things as they flow around its network. This step registers some serialisers
		// for triples and stuff like that
		Config config = new Config();
		JenaStormUtils.registerSerializers(config);
		
		// This bit creates an orchestrator from the builder and the query we defined above
		StormSPARQLReteTopologyOrchestrator orchestrator = StormSPARQLReteTopologyOrchestrator.createTopologyBuilder(
				config ,
				builder,
				FileUtils.readFileToString(query));
		// This bit gets the topology
		StormTopology top = orchestrator.buildTopology();
		
		// The rest of this stuff is specifics about storm's local cluster mode
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("top", config, top);
		Utils.sleep(20000);
		cluster.shutdown();
	}

}
