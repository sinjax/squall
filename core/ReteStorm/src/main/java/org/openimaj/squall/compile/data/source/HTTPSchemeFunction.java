package org.openimaj.squall.compile.data.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import org.mortbay.io.RuntimeIOException;
import org.openimaj.io.HttpUtils;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class HTTPSchemeFunction implements Function<URI, InputStream> {

	@Override
	public InputStream apply(URI in) {
		try {
			return HttpUtils.readURLAsStream(in.toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

}
