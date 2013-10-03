package org.openimaj.rdf.storm.tool.lang;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openimaj.kestrel.KestrelTupleWriter;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.openimaj.rdf.storm.topology.RuleReteStormTopologyFactory;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

import backtype.storm.generated.StormTopology;

/**
 * Given a string which constitutes a RIF rule set, construct
 * a storm topology using {@link RuleReteStormTopologyFactory}
 * @author David Monks <david.monks@zepler.net>
 */
public class RIFRuleLanguageHandler extends BaseRuleLanguageHandler {

	Logger logger = Logger.getLogger(RIFRuleLanguageHandler.class);

	@Override
	public StormTopology constructTopology(ReteStormOptions options) {
		RuleReteStormTopologyFactory factory = new RuleReteStormTopologyFactory(options.prepareConfig(), options.getRules());
		String inputQueue = options.inputQueue;
		String outputQueue = options.outputQueue;
		StormTopology topology = null;
		try {
			topology = factory.buildTopology(options.kestrelSpecList.get(0), inputQueue, outputQueue); // FIXME
		} catch (Exception e) {
			logger.error("Couldn't construct topology: " + e.getMessage());
			e.printStackTrace();
		}
		return topology;
	}

	@Override
	public KestrelTupleWriter tupleWriter(ArrayList<URL> urlList) throws IOException {
		return new NTripleKestrelTupleWriter(urlList);
	}

}
