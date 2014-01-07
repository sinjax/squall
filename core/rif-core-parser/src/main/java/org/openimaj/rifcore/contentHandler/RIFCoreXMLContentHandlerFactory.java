package org.openimaj.rifcore.contentHandler;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFCoreXMLContentHandlerFactory implements RIFXMLContentHandlerFactory {

	@Override
	public RIFCoreXMLContentHandler newHandler(){
		return new RIFCoreXMLContentHandler();
	}
	
}
