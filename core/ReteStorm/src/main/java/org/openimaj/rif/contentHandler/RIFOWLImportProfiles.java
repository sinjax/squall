package org.openimaj.rif.contentHandler;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFOWLImportProfiles extends RIFImportProfiles {

	/**
	 * 
	 */
	public RIFOWLImportProfiles(){
		super();
		try {
			this.put(new URI("http://www.w3.org/ns/entailment/Simple"), new SimpleNTriplesImportHandler()); // TODO make Simple entailments ContentHandler
			this.put(new URI("http://www.w3.org/ns/entailment/RDF"), null); // TODO make RDF entailments ContentHandler
			this.put(new URI("http://www.w3.org/ns/entailment/RDFS"), null); // TODO make RDFS entailments ContentHandler
			this.put(new URI("http://www.w3.org/ns/entailment/D"), null); // TODO make D entailments ContentHandler
			this.put(new URI("http://www.w3.org/ns/entailment/OWL-Direct"), null); // TODO make OWL Direct entailments ContentHandler
			this.put(new URI("http://www.w3.org/ns/entailment/OWL-RDF-Based"), null); // TODO make OWL RDF Based entailments ContentHandler
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
