package org.openimaj.rif.contentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.openimaj.rif.RIFRuleSet;
import org.xml.sax.SAXException;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFEntailmentImportProfiles extends RIFImportProfiles <RIFEntailmentImportHandler> {

	/**
	 * 
	 */
	public RIFEntailmentImportProfiles(){
		super();
		try {
			this.put(new URI("http://www.w3.org/ns/entailment/Core"), new RIFXMLImportHandler(new RIFCoreXMLContentHandlerFactory()));
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
	 * @param is
	 * @param prof
	 * @return 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public RIFRuleSet parse(InputStream is, URI prof) throws IOException, SAXException{
		return this.get(prof).importToRuleSet(is, new RIFRuleSet(prof,this));
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
		RIFEntailmentImportHandler rifEntailmentImportHandler = this.get(prof);
		return rifEntailmentImportHandler.importToRuleSet(loc, ruleSet);
	}
	
}
