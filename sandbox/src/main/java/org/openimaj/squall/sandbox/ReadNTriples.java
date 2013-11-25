package org.openimaj.squall.sandbox;

import java.net.URI;
import java.net.URISyntaxException;

import org.openimaj.squall.compile.data.source.URIProfileISourceFactory;
import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.Stream;

public class ReadNTriples {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {
		ISource<Stream<Context>> wang = URIProfileISourceFactory.instance().createSource(new URI("file:///Users/david.monks/Downloads/lsbench/rdfPostStream1000.nt"), new URI("http://www.w3.org/ns/stream/Turtle"));
		
		wang.setup();
		
		Stream<Context> strm = wang.apply();
		strm.forEach(new Operation<Context>() {
			
			@Override
			public void perform(Context object) {
				System.out.println(object);
			}
		});
	}

}
