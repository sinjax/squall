package org.openimaj.squall.tool;

import java.io.IOException;

import org.bouncycastle.util.Strings;
import org.junit.Test;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestSquallTool {
	
	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testSquallTool() throws IOException{
		SquallTool.main(toArgs(""
		+ "-tm RIF "
			+ " -rifr java:///queries.rif"
		+ " -pm GREEDYCS"
		+ " -bm STORM"
			+ " -stm LOCAL"
				+ " -st 5000"
		));
	}

	private String[] toArgs(String string) {
		return string.replaceAll("[ ]+", " ").split(" ");
	}

}
