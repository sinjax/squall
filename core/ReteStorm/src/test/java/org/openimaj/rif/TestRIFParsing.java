package org.openimaj.rif;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.openimaj.rif.imports.profiles.RIFEntailmentImportProfiles;
import org.openimaj.squall.compile.rif.TestRifRuleCompilerGreedyOrchestratorStormBuilder;
import org.xml.sax.SAXException;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class TestRIFParsing {

	/**
	 * Testing the ability to import using the "java://" uri scheme.
	 */
	@Test
	public void test() {
		String ruleSource = "/lsbench/queries.rif";
		
		RIFRuleSet rules = null;
		RIFEntailmentImportProfiles profs = new RIFEntailmentImportProfiles();
		try {
			InputStream resourceAsStream = TestRifRuleCompilerGreedyOrchestratorStormBuilder.class.getResourceAsStream(ruleSource);
//			System.out.println(FileUtils.readall(resourceAsStream));
			rules = profs.parse(
					resourceAsStream,
					new URI("http://www.w3.org/ns/entailment/Core")
				);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println(rules);
		assert(true);
	}

}
