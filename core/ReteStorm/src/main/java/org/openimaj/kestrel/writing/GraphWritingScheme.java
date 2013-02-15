/**
// * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.kestrel.writing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mortbay.io.RuntimeIOException;
import org.openimaj.io.WriteableBinary;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt;
import org.openimaj.rdf.storm.topology.bolt.StormReteBolt.Component;
import org.openjena.riot.RiotWriter;

import backtype.storm.tuple.Fields;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Writes and reads the tuples described by the
 * {@link StormReteBolt#declaredFields(int)} function
 * with 0 as the parameter
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class GraphWritingScheme implements WritingScheme {

	Map<Component, WriteableBinary> writers = new HashMap<StormReteBolt.Component, WriteableBinary>();

	private static final Logger logger = Logger.getLogger(GraphWritingScheme.class);

	/**
	 *
	 */
	private static final long serialVersionUID = -2734506908903229738L;

	@Override
	public byte[] serialize(List<Object> objects) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeBoolean(StormReteBolt.extractIsAdd(objects));
			dos.writeLong(StormReteBolt.extractTimestamp(objects));
			RiotWriter.writeTriples(dos, StormReteBolt.extractGraph(objects));
		} catch (IOException e) {
			logger.error("Couldn't write isAdd" + e.getMessage());
		}
		try {
			baos.flush();
		} catch (IOException e) {
			logger.error("Couldn't flush to write object");
		}
		return baos.toByteArray();
	}

	@Override
	public List<Object> deserialize(byte[] ser) {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ser));
		try {
			boolean isAdd = dis.readBoolean();
			long timeStamp = dis.readLong();
			//			final Graph g = GraphFactory.createGraphMem();
			//			LangNTriples parser = RiotReader.createParserNTriples(dis, new Sink<Triple>() {
			//
			//				@Override
			//				public void close() {
			//					// TODO Auto-generated method stub
			//
			//				}
			//
			//				@Override
			//				public void send(Triple item) {
			//					g.add(item);
			//				}
			//
			//				@Override
			//				public void flush() {
			//					// TODO Auto-generated method stub
			//
			//				}
			//			});
			//			parser.parse();
			Model m = ModelFactory.createDefaultModel();
			m.read(dis, "", "N-TRIPLES");
			return StormReteBolt.asValues(isAdd, m.getGraph(), timeStamp);
		} catch (IOException e) {
			logger.error("Couldn't read graph!" + e.getMessage());
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public Fields getOutputFields() {
		return StormReteBolt.declaredFields(0);
	}
}
