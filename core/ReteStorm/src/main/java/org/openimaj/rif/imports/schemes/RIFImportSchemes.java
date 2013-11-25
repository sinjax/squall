package org.openimaj.rif.imports.schemes;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

import org.openimaj.util.function.Function;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFImportSchemes extends HashMap<String, Function<URI, InputStream>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3179254634773998638L;

	/**
	 * 
	 */
	public RIFImportSchemes(){
		this.put("http", new HTTPSchemeFunction());
		this.put("file", new URLSchemeFunction());
		this.put("java", new JavaSchemeFunction());
	}
	
}
