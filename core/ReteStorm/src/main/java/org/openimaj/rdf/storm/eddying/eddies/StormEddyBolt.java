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
package org.openimaj.rdf.storm.eddying.eddies;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter;
import org.openimaj.rdf.storm.eddying.routing.StormGraphRouter.Action;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt;
import org.openimaj.rdf.storm.eddying.stems.StormSteMBolt.Component;

import com.hp.hpl.jena.graph.Graph;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

/**
 * @author davidlmonks
 *
 */
public class StormEddyBolt implements IRichBolt {

	private static final long serialVersionUID = 1073714124183765931L;
	private static Logger logger = Logger.getLogger(StormEddyBolt.class);

	public static final String STREAM_TO_EDDY = "eddy stream";
	
	protected StormGraphRouter router;
	
	/**
	 * @param sgr
	 */
	public StormEddyBolt(StormGraphRouter sgr){
		this.router = sgr;
	}
	
	private Map<String,Object> conf;
	private TopologyContext context;
	private OutputCollector collector;
	
	@SuppressWarnings("unchecked")
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map stormConf,
						TopologyContext context,
						OutputCollector collector) {
		this.conf = stormConf;
		this.context = context;
		this.collector = collector;
		
		this.router.setOutputCollector(collector);
	}

	@Override
	public void execute(Tuple input) {
		this.router.routeGraph(
					 /*anchor*/input,
					 /*action*/(Action)input.getValueByField(Component.action.toString()),
					  /*isAdd*/input.getBooleanByField(StormSteMBolt.Component.isAdd.toString()),
					  /*graph*/(Graph)input.getValueByField(StormSteMBolt.Component.graph.toString()),
				  /*timestamp*/input.getLongByField(Component.timestamp.toString())
		);
	}
	
	@Override
	public void cleanup() {
		this.conf = null;
		this.context = null;
		this.collector = null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		this.router.declareOutputFields(declarer);
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return conf;
	}

}