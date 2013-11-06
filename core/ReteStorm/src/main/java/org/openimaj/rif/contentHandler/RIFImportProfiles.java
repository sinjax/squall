package org.openimaj.rif.contentHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.openimaj.rif.RIFRuleSet;
import org.xml.sax.SAXException;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public abstract class RIFImportProfiles extends HashMap<URI, RIFImportHandler> {

	/**
	 * 
	 */
	public RIFImportProfiles(){
		super();
		try {
			this.put(new URI("http://www.w3.org/ns/entailment/Core"), new RIFXMLImportHandler(new RIFCoreXMLContentHandler()));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param loc
	 * @param prof
	 * @return 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public RIFRuleSet parse(URI loc, URI prof) throws IOException, SAXException{
		return this.get(prof).importToRuleSet(loc, new RIFRuleSet(prof,this));
	}
	
	/**
	 * @param loc
	 * @param prof
	 * @param ruleSet 
	 * @return 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public RIFRuleSet parse(URI loc, URI prof, RIFRuleSet ruleSet) throws IOException, SAXException{
		return this.get(prof).importToRuleSet(loc, ruleSet);
	}
	
}
