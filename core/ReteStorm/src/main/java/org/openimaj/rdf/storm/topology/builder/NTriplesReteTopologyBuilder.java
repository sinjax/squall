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
package org.openimaj.rdf.storm.topology.builder;

import org.openimaj.rdf.storm.spout.NTripleSpout;
import org.openimaj.rdf.storm.spout.NTriplesSpout;

import backtype.storm.topology.TopologyBuilder;

/**
 * The {@link NTriplesReteTopologyBuilder} provides triples froma URI via
 * the {@link NTriplesSpout}.
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class NTriplesReteTopologyBuilder extends BaseReteTopologyBuilder {
	/**
	 * The name of the spout outputting triples
	 */
	public static final String TRIPLE_SPOUT = "tripleSpout";
	private String nTriples;

	/**
	 * @param nTriplesURI the source of the nTriples
	 */
	public NTriplesReteTopologyBuilder(String nTriplesURI) {
		this.nTriples = nTriplesURI;
	}

	@Override
	public String prepareSourceSpout(TopologyBuilder builder) {
		NTripleSpout tripleSpout = new NTripleSpout(nTriples);
		builder.setSpout(TRIPLE_SPOUT, tripleSpout, 1);
		return TRIPLE_SPOUT;
	}

	@Override
	public void finaliseTopology(ReteTopologyBuilderContext context) {
		// TODO Auto-generated method stub
		
	}

}
