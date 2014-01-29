package org.openimaj.squall.compile.data.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.storm.utils.KestrelParsedURI;
import org.openimaj.storm.utils.KestrelUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.stream.KestrelStream;
import org.openimaj.util.stream.KestrelWriter;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelSchemeFunction implements Function<URI, Stream<Context>> {
	private static final Logger logger = Logger.getLogger(KestrelSchemeFunction.class);
	// Kestrel URIs follow Apache camel format: kestrel://kserver01:22133,kserver02:22133,kserver03:22133/massive
	@Override
	public Stream<Context> apply(URI in) {
		KestrelParsedURI hostQueue = KestrelUtils.parseKestrelURI(in);
		
		KestrelStream<Context> kestrelStream = new KestrelStream<Context>(hostQueue,new MultiFunction<byte[],Context>(){

			@Override
			public List<Context> apply(byte[] in) {
				Collection<Triple> triples = JenaUtils.readNTriples(new ByteArrayInputStream(in));
				List<Context> ret = new ArrayList<Context>();
				for (Triple triple : triples) {
					ret.add(new Context(ContextKey.TRIPLE_KEY.toString(),triple));
				}
				
				return ret;
			}
			
		});
		Map<String, List<String>> parsed = hostQueue.params;
		// Delete the queue if you need to
		if(parsed.containsKey("predelete") && Boolean.parseBoolean(parsed.get("predelete").get(0))){
			try {
				KestrelUtils.deleteQueues(in);
			} catch (TException e) {
			}
		}
		// Deal with preloading!
		if(parsed.containsKey("preload")){
			Lang l = Lang.TURTLE;
			if(parsed.containsKey("preloadlang")){				
				l = RDFLanguages.nameToLang(parsed.get("preloadlang").get(0));
			}
			KestrelWriter op = new KestrelWriter(in);
			op.setup();
			for (String context : parsed.get("preload")) {
				try {
					URI preload = new URI(context);
					preloadURI(preload,l,op);
				} catch (URISyntaxException e) {
					logger.debug("Could not parse URI for preloading: " + context);
				}				
			}
			op.cleanup();
		}
		return kestrelStream;
	}
	private void preloadURI(final URI preload, final Lang l, final KestrelWriter op) {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				URIProfileISourceFactory fact = URIProfileISourceFactory.instance();
				ISource<Stream<Context>> isource = fact.createSource(preload, l);
				isource.setup();
				isource.apply()
				.map(new Function<Context, byte[]>() {

					@Override
					public byte[] apply(Context in) {
						Graph graph = GraphFactory.createGraphMem();
						Triple typed = in.getTyped(ContextKey.TRIPLE_KEY.toString());
						graph.add(typed);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						RDFDataMgr.write(baos, graph, Lang.NTRIPLES);
						return baos.toByteArray();
					}
				})
				.forEach(op);
				isource.cleanup();
			}
			
		};
		r.run();
		
	}
	private Map<String, List<String>> splitQuery(URI uri) throws UnsupportedEncodingException {
	    Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
	    String query = uri.getQuery();
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
	

}
