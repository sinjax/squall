package org.openimaj.rdf.storm.tool.lang;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;
import org.openimaj.kestrel.KestrelTupleWriter;
import org.openimaj.rdf.storm.sparql.topology.bolt.sink.QuerySolutionSerializer;
import org.openimaj.rdf.storm.tool.ReteStormOptions;

import backtype.storm.generated.StormTopology;

/**
 * 
 * @author David Monks <david.monks@zepler.net>
 */
public class OntologyRuleLanguageHandler extends BaseRuleLanguageHandler {

	private enum Dialect {
		/**
		 * RDF Schema rules
		 */
		RDFS ("")
		,
		/**
		 * OWL 2 RL dialect rules
		 */
		OWL2RL ("")
		;
		
		private final String uri;
		
		/**
		 * Read rules from file at URI address (this can be locally scoped with "file://...").
		 * @param address is the string address of a file containing a RIF rule set.
		 */
		private Dialect (String address) {
			 this.uri = address;
		}
		
		/**
		 * Returns the URI of the rules describing the dialect.
		 * @return The URI of the file containing the RIF rules describing the dialect.
		 */
		public String getURI(){
			return this.uri;
		}
	}
	
	@Option(
			name = "--reasoning-dialect",
			aliases = "-rd",
			required = true,
			usage = "The reasoning dialect against which to process the provided ontology in order to generate ABox rules from its TBox.")
	private Dialect dialect;
	
	@Override
	public StormTopology constructTopology(ReteStormOptions options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KestrelTupleWriter tupleWriter(ArrayList<URL> urlList)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
