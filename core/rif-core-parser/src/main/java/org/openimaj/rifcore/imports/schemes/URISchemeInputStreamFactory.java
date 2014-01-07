package org.openimaj.rifcore.imports.schemes;

import java.io.InputStream;
import java.net.URI;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public interface URISchemeInputStreamFactory {
	
	/**
	 * @param source
	 * @return
	 */
	public InputStream getInputStream(URI source);

}
