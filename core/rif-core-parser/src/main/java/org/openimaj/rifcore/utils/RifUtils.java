package org.openimaj.rifcore.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.openimaj.rifcore.RIFRuleSet;
import org.openimaj.rifcore.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.rifcore.imports.schemes.RIFImportSchemes;
import org.xml.sax.SAXException;


/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * Some helper functions for RIF
 */
public class RifUtils {
	
	/**
	 * @param ruleSource
	 * @return
	 */
	public static RIFRuleSet readRules(String ruleSource) {
		try {
			return readRules(new URI(ruleSource));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param ruleSource
	 * @return
	 */
	public static RIFRuleSet readRules(URI ruleSource) {
		try {
			return readRules(ruleSource, new URI("http://www.w3.org/ns/entailment/Core"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param ruleSource
	 * @param profile 
	 * @return
	 */
	public static RIFRuleSet readRules(String ruleSource, URI profile) {
		try {
			return readRules(new URI(ruleSource), profile);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param ruleSource
	 * @param profile
	 * @return
	 */
	public static RIFRuleSet readRules(URI ruleSource, URI profile) {
		RIFEntailmentImportProfiles profs = new RIFEntailmentImportProfiles();
		RIFImportSchemes ris = new RIFImportSchemes();
		InputStream is = ris.get(ruleSource.getScheme()).getInputStream(ruleSource);
		RIFRuleSet rules = null;
		try {
			rules = profs.parse(
					is,
					profile
				);
		} catch (IOException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rules;
	}
	
}
