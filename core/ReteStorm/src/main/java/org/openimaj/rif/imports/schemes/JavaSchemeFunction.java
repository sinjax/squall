package org.openimaj.rif.imports.schemes;

import java.io.InputStream;
import java.net.URI;

import org.openimaj.util.function.Function;

/**
 * The java scheme uses {@link Class#getResourceAsStream(String)} to 
 * get an input stream. This is primarily useful in a testing context
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JavaSchemeFunction implements Function<URI, InputStream> {

	@Override
	public InputStream apply(URI in) {
		String path = in.getPath();
		return JavaSchemeFunction.class.getResourceAsStream(path);
	}

}
