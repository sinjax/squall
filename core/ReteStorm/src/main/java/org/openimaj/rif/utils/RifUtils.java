package org.openimaj.rif.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.openimaj.rif.RIFRuleSet;
import org.openimaj.rif.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.rif.imports.schemes.RIFImportSchemes;
import org.xml.sax.SAXException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Some helper functions for jena
 *
 */
public class RifUtils {
	
	public static RIFRuleSet readRules(String ruleSource) {
		try {
			return readRules(new URI(ruleSource));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	public static RIFRuleSet readRules(URI ruleSource) {
		RIFEntailmentImportProfiles profs = new RIFEntailmentImportProfiles();
		RIFImportSchemes ris = new RIFImportSchemes();
		InputStream is = ris.get(ruleSource.getScheme()).apply(ruleSource);
		RIFRuleSet rules = null;
		try {
			rules = profs.parse(
					is,
					new URI("http://www.w3.org/ns/entailment/Core")
				);
		} catch (IOException | SAXException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rules;
	}
	
}
