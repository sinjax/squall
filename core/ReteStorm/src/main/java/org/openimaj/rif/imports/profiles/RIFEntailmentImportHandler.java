package org.openimaj.rif.imports.profiles;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.openimaj.rif.RIFRuleSet;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public interface RIFEntailmentImportHandler {
	
	/**
	 * @param loc
	 * @param ruleSet
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public RIFRuleSet importToRuleSet(URI loc, RIFRuleSet ruleSet) throws SAXException, IOException;
	
	/**
	 * 
	 * @param loc
	 * @param ruleSet
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public RIFRuleSet importToRuleSet(InputStream loc, RIFRuleSet ruleSet) throws SAXException, IOException;
	
	/**
	 * @param loc
	 * @param ruleSet
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public RIFRuleSet importToRuleSet(InputSource loc, RIFRuleSet ruleSet) throws SAXException, IOException;
	
}
