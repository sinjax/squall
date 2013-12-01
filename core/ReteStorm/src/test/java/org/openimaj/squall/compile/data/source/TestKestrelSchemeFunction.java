package org.openimaj.squall.compile.data.source;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.thrift7.TException;
import org.junit.Test;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.storm.utils.KestrelUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.KestrelWriter;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestKestrelSchemeFunction {
	/**
	 * @throws URISyntaxException 
	 * @throws TException 
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testLocalKestrel() throws URISyntaxException, TException{
		URIProfileISourceFactory fact = URIProfileISourceFactory.instance();
		URI host = new URI("kestrel://localhost/testQueue?preload=java:///test.rdfs&predelete=true");
		
		ISource<Stream<Context>> source = fact.createSource(host, (Lang)null);
		Stream<Context> strm = source.apply();
		for (int i = 0; i < 100; i++) {
			System.out.println(strm.next());
		}
	}
}
