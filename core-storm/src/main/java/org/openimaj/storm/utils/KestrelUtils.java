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
package org.openimaj.storm.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.util.pair.IndependentPair;

import backtype.storm.spout.KestrelThriftClient;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelUtils {

	/**
	 * @param spec
	 *            the server to connect to
	 * @param queues
	 *            the queues to expunge
	 * @throws TException
	 */
	public static void deleteQueues(KestrelServerSpec spec, String... queues) throws TException {
		final KestrelThriftClient client = new KestrelThriftClient(spec.host, spec.port);
		for (final String queue : queues) {
			client.delete_queue(queue);
		}
		client.close();
	}
	
	/**
	 *
	 * @param in
	 * @return turn {@link URI} into a list of hosts and a queue
	 */
	public static KestrelParsedURI parseKestrelURI(URI in) {
		String kestrelHostString = in.getAuthority();
		String[] parts = kestrelHostString.split(",");
		List<String> hostStrings = Arrays.asList(parts);
		List<KestrelServerSpec> hosts = KestrelServerSpec.parseKestrelAddressList(hostStrings);
		String queue = in.getPath().replace("/", "");
		
		KestrelParsedURI kURI = new KestrelParsedURI();
		kURI.hosts = hosts;
		kURI.queue = queue;
		try {
			kURI.params = parseQuery(in);
		} catch (UnsupportedEncodingException e) {
			kURI.params = new HashMap<String, List<String>>();
		}
		
//		IndependentPair<List<KestrelServerSpec>, String> hostQueue = IndependentPair.pair(hosts, queue);
		return kURI;
	}
	
	private static Map<String, List<String>> parseQuery(URI uri) throws UnsupportedEncodingException {
	    Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
	    String query = uri.getQuery();
	    if(query == null) return new HashMap<String, List<String>>();
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        String v = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
	        String k = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
			List<String> l = query_pairs.get(k);
			if(l == null){
				l = new ArrayList<String>();
				query_pairs.put(k, l);
			}
			l.add(v);			
	    }
	    return query_pairs;
	}

	public static void deleteQueues(URI host) throws TException {
		KestrelParsedURI pair = parseKestrelURI(host);
		for (KestrelServerSpec kss : pair.hosts) {			
			deleteQueues(kss, pair.queue);
		}
	}

}
