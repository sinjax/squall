package org.openimaj.rif.contentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openimaj.rif.RIFRuleSet;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFXMLImportHandler implements RIFEntailmentImportHandler {

	private RIFXMLContentHandlerFactory factory;
	
	/**
	 * @param factory 
	 */
	public RIFXMLImportHandler(RIFXMLContentHandlerFactory factory){
		this.factory = factory;
	}

	@Override
	public RIFRuleSet importToRuleSet(URI loc, RIFRuleSet ruleSet) throws SAXException, IOException {
		return importToRuleSet(new InputSource(loc.toASCIIString()), ruleSet);
	}

	@Override
	public RIFRuleSet importToRuleSet(InputStream loc, RIFRuleSet ruleSet) throws SAXException, IOException {
		return importToRuleSet(new InputSource(loc), ruleSet);
	}

	@Override
	public RIFRuleSet importToRuleSet(InputSource loc, RIFRuleSet ruleSet)
			throws SAXException, IOException {
		RIFXMLContentHandler conH = this.factory.newHandler();
		conH.setRuleSet(ruleSet);
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    try {
			spf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			spf.setFeature("http://xml.org/sax/features/namespaces", true);
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXNotRecognizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		try {
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
		    xmlReader.setContentHandler(conH);
		    xmlReader.parse(loc);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return conH.getRuleSet();
	}

}
