package org.openimaj.squall.functions.rif.sources;

import java.net.URI;
import java.net.URISyntaxException;

import org.openimaj.rif.contentHandler.RIFImportProfiles;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFStreamImportProfiles extends RIFImportProfiles <ISourceFactory> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3398968270761603709L;

	/**
	 * 
	 */
	public RIFStreamImportProfiles(){
		super();
		try {
			this.put(new URI("http://www.w3.org/ns/stream/NTriples"), new NTriplesISourceFactory()); // TODO: Construct IStream functions
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
