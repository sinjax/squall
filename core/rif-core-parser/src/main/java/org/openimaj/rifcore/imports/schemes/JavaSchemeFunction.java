package org.openimaj.rifcore.imports.schemes;

import java.io.InputStream;
import java.net.URI;

/**
 * The java scheme uses {@link Class#getResourceAsStream(String)} to 
 * get an input stream. This is primarily useful in a testing context
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JavaSchemeFunction implements URISchemeInputStreamFactory {

	@Override
	public InputStream getInputStream(URI in) {
		String path = in.getPath();
		return JavaSchemeFunction.class.getResourceAsStream(path);
	}

}
