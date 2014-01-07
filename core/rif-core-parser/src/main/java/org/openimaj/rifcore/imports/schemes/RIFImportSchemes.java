package org.openimaj.rifcore.imports.schemes;

import java.util.HashMap;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFImportSchemes extends HashMap<String, URISchemeInputStreamFactory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3179254634773998638L;

	/**
	 * 
	 */
	public RIFImportSchemes(){
		this.put("http", new HTTPSchemeFunction());
		this.put("file", new FileSchemeFunction());
		this.put("java", new JavaSchemeFunction());
	}
	
}
