package org.openimaj.rifcore.imports.schemes;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import org.openimaj.io.HttpUtils;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class HTTPSchemeFunction implements URISchemeInputStreamFactory {

	@Override
	public InputStream getInputStream(URI in) {
		try {
			return HttpUtils.readURLAsStream(in.toURL());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
