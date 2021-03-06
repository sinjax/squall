/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.topology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.topology.bolt.ReteConflictSetBolt;
import org.openimaj.rdf.storm.topology.builder.ConfigurableRuleReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.KestrelReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.NTriplesReteTopologyBuilder;
import org.openimaj.rdf.storm.topology.builder.ReteTopologyBuilder;
import org.openimaj.rdf.storm.utils.JenaStormUtils;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.scheduler.Cluster;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a set of rules, construct a RETE topology such that filter (alpha)
 * nodes and join (beta) nodes are filtering bolts
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RuleReteStormTopologyFactory {
	/**
	 * The name of a debug bolt
	 */
	public static final String DEBUG_BOLT = "debugBolt";

	/**
	 * default rules
	 */
	public static final String RDFS_RULES = "/org/openimaj/rdf/rules/rdfs-fb-tgc-noresource.rules";
	/**
	 * the name of the final bolt
	 */
	public static final String FINAL_TERMINAL = "final_term";
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RuleReteStormTopologyFactory.class);

	private InputStream rulesStream;

	private String rulesString;

	/**
	 * Construct a Rete topology using the default RDFS rules
	 *
	 * @param conf
	 *            the {@link Config} to be sent to the {@link Cluster}. Only
	 *            used to register serialisers
	 */
	public RuleReteStormTopologyFactory(Config conf) {
		JenaStormUtils.registerSerializers(conf);
		this.rulesStream = RuleReteStormTopologyFactory.class.getResourceAsStream(RDFS_RULES);
	}

	/**
	 * Construct a Rete topology using the InputStream as a source of rules
	 *
	 * @param conf
	 *
	 * @param rulesStream
	 *            the stream of rules
	 */
	public RuleReteStormTopologyFactory(Config conf, InputStream rulesStream) {
		JenaStormUtils.registerSerializers(conf);
		this.rulesStream = rulesStream;
	}

	/**
	 * @param conf
	 * @param rules
	 */
	public RuleReteStormTopologyFactory(Config conf, String rules) {
		JenaStormUtils.registerSerializers(conf);
		this.rulesStream = null;
		this.rulesString = rules;
	}

	/**
	 * Using specified rules, construct a RETE storm topology
	 *
	 * @param nTriples
	 *            A URL containing nTriples
	 *
	 * @return a storm topology
	 * @throws IOException
	 */
	public StormTopology buildTopology(String nTriples) throws IOException {
		final TopologyBuilder builder = new TopologyBuilder();
		final List<Rule> rules = loadRules();
		final ReteTopologyBuilder topologyBuilder = new NTriplesReteTopologyBuilder(nTriples);
		topologyBuilder.compile(builder, rules);
		return builder.createTopology();
	}

	/**
	 * Using specified rules, construct a RETE storm topology
	 *
	 * @param spec
	 *            The kestrel server to which to connect
	 * @param inputQueue
	 *            String outputQueue A kestrel queue containing triples
	 * @param outputQueue
	 *            the name of the output queue
	 *
	 * @return a storm topology
	 * @throws IOException
	 */
	public StormTopology buildTopology(KestrelServerSpec spec, String inputQueue, String outputQueue) throws IOException {

		final TopologyBuilder builder = new TopologyBuilder();
		final List<Rule> rules = loadRules();
		final ReteTopologyBuilder topologyBuilder = new KestrelReteTopologyBuilder(spec, inputQueue, outputQueue);
		topologyBuilder.compile(builder, rules);

		return builder.createTopology();
	}

	public StormTopology buildTopology(IRichSpout source, ReteConflictSetBolt terminal) throws IOException{
		final TopologyBuilder builder = new TopologyBuilder();
		final List<Rule> rules = loadRules();
		final ReteTopologyBuilder topologyBuilder = new ConfigurableRuleReteTopologyBuilder(source,terminal);
		topologyBuilder.compile(builder, rules);
		return builder.createTopology();
	}

	/**
	 * @param topologyBuilder
	 *
	 * @return given a {@link ReteTopologyBuilder} and a list of
	 *         {@link RuleReteStormTopologyFactory} instances construct a
	 *         {@link StormTopology}
	 * @throws IOException
	 */
	public StormTopology buildTopology(ReteTopologyBuilder topologyBuilder) throws IOException {
		final TopologyBuilder builder = new TopologyBuilder();
		topologyBuilder.compile(builder, loadRules());
		final StormTopology top = builder.createTopology();
		return top;
	}

	/**
	 * @param config
	 *            the {@link Config} instance with which the
	 *            {@link StormTopology} will be submitted to the {@link Cluster}
	 *            .
	 * @param topologyBuilder
	 *            the approach to constructing a {@link StormTopology}
	 * @param rules
	 *            the rules to construct the rete network with
	 * @return given a {@link TopologyBuilder} and a source for {@link Rule}
	 *         instances build {@link StormTopology}
	 * @throws IOException
	 */
	public static StormTopology buildTopology(Config config, ReteTopologyBuilder topologyBuilder, InputStream rules)
			throws IOException
	{
		final RuleReteStormTopologyFactory topology = new RuleReteStormTopologyFactory(config, rules);
		return topology.buildTopology(topologyBuilder);
	}

	private List<Rule> loadRules() throws IOException {
		List<Rule> rules;
		if (this.rulesStream != null) {
			rules = Rule.parseRules(Rule
					.rulesParserFromReader(new BufferedReader(new InputStreamReader(this.rulesStream))));
		} else if (this.rulesString != null) {
			rules = Rule.parseRules(Rule.rulesParserFromReader(new BufferedReader(new StringReader(this.rulesString))));
		} else {
			throw new IOException("Couldn't load rules!");
		}
		return rules;
	}

	/**
	 * run the rete topology
	 *
	 * @param args
	 * @throws InvalidTopologyException
	 * @throws AlreadyAliveException
	 * @throws IOException
	 * @throws TException
	 */
	public static void main(String args[]) throws AlreadyAliveException, InvalidTopologyException, TException,
			IOException
	{
		final String rdfSource = "file:///Users/ss/Development/java/openimaj/trunk/storm/ReteStorm/src/test/resources/test.rdfs";
		final String ruleSource = "/Users/ss/Development/java/openimaj/trunk/storm/ReteStorm/src/test/resources/test.rules";

		final Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(2);
		conf.setMaxSpoutPending(1);
		conf.setFallBackOnJavaSerialization(false);
		conf.setSkipMissingKryoRegistrations(false);
		final RuleReteStormTopologyFactory reteTopology = new RuleReteStormTopologyFactory(conf, new FileInputStream(
				ruleSource));
		final LocalCluster cluster = new LocalCluster();
		final StormTopology topology = reteTopology.buildTopology(rdfSource);
		// KestrelServerSpec spec = KestrelServerSpec.localThrift();
		// String inputQueue = "triples";
		// String outputQueue = "processedTriples";
		// KestrelUtils.deleteQueues(spec, inputQueue, outputQueue);
		// NTripleKestrelTupleWriter rdfWriter = new
		// NTripleKestrelTupleWriter(new URL(rdfSource));
		// rdfWriter.write(spec, inputQueue, outputQueue);
		// StormTopology topology = reteTopology.buildTopology(spec, inputQueue,
		// outputQueue);
		cluster.submitTopology("reteTopology", conf, topology);

		// Thread t = new Thread(new KestrelQueuePrinter(spec, outputQueue));
		// t.setDaemon(true);
		// t.start();

		Utils.sleep(10000);
		cluster.killTopology("reteTopology");
		cluster.shutdown();

	}
}
