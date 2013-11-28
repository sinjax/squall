/**
 * Copyright (c) ${year}, The University of Southampton and the individual contributors.
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
package org.openimaj.rdf.storm.spout;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.riot.Lang;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.storm.spout.SimpleSpout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;

/**
 * Given a URL, This spout creates a stream of triples formatted to Storm fields according to a Jena RETE <-> Storm translator.
 * Based on the {@link NTriplesSpout} by Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class NTripleSpout extends SimpleSpout {
	
	public static final String TRIPLES_FIELD = "triples";

	private String nTriplesURL;

	private Iterator<Triple> iter;

	/**
	 * @param nTriplesURL
	 *            source of the ntriples
	 *
	 */
	public NTripleSpout(String nTriplesURL) {
		this.nTriplesURL = nTriplesURL;
	}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		super.open(conf, context, collector);
		URL url;
		try {
			url = new URL(this.nTriplesURL);
			iter = JenaUtils.createIterator(url.openStream(), Lang.NTRIPLES);
		} catch (Exception e) {
		}
	}

	@Override
	public void nextTuple() {
		if (iter.hasNext()) {
			Triple parsed = iter.next();
			Graph graph = new GraphMem();
			graph.add(parsed);
			try {
				this.collector.emit(StormReteBolt.asValues(true,graph,0l,parsed));
			} catch (Exception e) {

			}
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(StormReteBolt.declaredFields(TRIPLES_FIELD));
	}
	
	/**
	 * Given a Jena {@link Triple} construct a {@link Values} instance which is
	 * the subject, predicate and value of the triple calling
	 * {@link Node#toString()}
	 * 
	 * @param t
	 * @return a Values instances
	 */
	public static Values asValue(Triple t) {
		Graph graph = new GraphMem();
		graph.add(t);
		return StormReteBolt.asValues(true, graph, 0l, t);
	}

	@Override
	public void close() {
		super.close();
	}

	public static Triple asTriple(Tuple input) {
		try {
			return (Triple)input.getValueByField(TRIPLES_FIELD);
		} catch (Exception e){
			return null;
		}
	}

}