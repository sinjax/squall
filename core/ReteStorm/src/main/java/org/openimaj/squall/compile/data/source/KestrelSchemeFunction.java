package org.openimaj.squall.compile.data.source;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.storm.utils.KestrelUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.KestrelStream;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelSchemeFunction implements Function<URI, Stream<Context>> {
	private static final Logger logger = Logger.getLogger(KestrelSchemeFunction.class);
	// Kestrel URIs follow Apache camel format: kestrel://kserver01:22133,kserver02:22133,kserver03:22133/massive
	@Override
	public Stream<Context> apply(URI in) {
		IndependentPair<List<KestrelServerSpec>, String> hostQueue = KestrelUtils.uriToHostsQueue(in);
		
		return new KestrelStream<Context>(hostQueue.firstObject(),hostQueue.secondObject(),new MultiFunction<byte[],Context>(){

			@Override
			public List<Context> apply(byte[] in) {
				Collection<Triple> triples = JenaUtils.readNTriples(new ByteArrayInputStream(in));
				List<Context> ret = new ArrayList<Context>();
				for (Triple triple : triples) {
					ret.add(new Context("triple",triple));
				}
				
				return ret;
			}
			
		});
	}
	

}
