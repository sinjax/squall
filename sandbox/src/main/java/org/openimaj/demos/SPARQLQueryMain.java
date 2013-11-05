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
	
	public static void main(String[] args) throws TranslationException, IOException {
		File query = new File(args[0]);
		File output = new File(args[1]);
		
		StaticDataFileNTriplesSPARQLReteTopologyBuilder builder = new StaticDataFileNTriplesSPARQLReteTopologyBuilder(output);
		Config config = new Config();
		JenaStormUtils.registerSerializers(config);
		StormSPARQLReteTopologyOrchestrator orchestrator = StormSPARQLReteTopologyOrchestrator.createTopologyBuilder(
				config ,
				builder,
				FileUtils.readFileToString(query));
		StormTopology top = orchestrator.buildTopology();
		
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("top", config, top);
		Utils.sleep(20000);
		cluster.shutdown();
	}

}
