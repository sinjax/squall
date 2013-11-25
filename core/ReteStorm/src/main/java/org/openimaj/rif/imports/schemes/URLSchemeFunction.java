package org.openimaj.rif.imports.schemes;

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
public class URLSchemeFunction implements Function<URI, InputStream> {

	@Override
	public InputStream apply(URI in) {
		try {
			return in.toURL().openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
