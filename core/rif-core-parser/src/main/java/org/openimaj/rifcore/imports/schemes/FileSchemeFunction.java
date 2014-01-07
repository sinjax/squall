package org.openimaj.rifcore.imports.schemes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class FileSchemeFunction implements URISchemeInputStreamFactory {

	@Override
	public InputStream getInputStream(URI in) {
		String path = in.getHost() == null ? in.getPath() : in.getHost() + in.getPath();
		File f = new File(path);
		try {
			return new FileInputStream(f);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
