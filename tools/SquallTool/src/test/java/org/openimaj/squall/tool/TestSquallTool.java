package org.openimaj.squall.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.riot.Lang;
import org.bouncycastle.util.Strings;
import org.junit.Test;
import org.openimaj.squall.utils.JenaUtils;

import com.hp.hpl.jena.graph.Triple;

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
				+ " -st 10000"
		));
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testSquallToolFileOutput() throws IOException{
		File tmp = File.createTempFile("out", ".nt");
		SquallTool.main(toArgs(""
			+ "-tm RIF "
				+ " -rifr java:///queries.rif"
			+ " -pm GREEDYCS"
			+ " -bm STORM"
				+ " -stm LOCAL"
					+ " -st 10000" 
			+ " -o TRIPLEFILE" 
				+ " -of " + tmp.getAbsolutePath()
		));
		Iterator<Triple> iter = JenaUtils.createIterator(new FileInputStream(tmp), Lang.NTRIPLES);
		while(iter.hasNext()){
			System.out.println(iter.next());
		}
		
		tmp.delete();
	}

	private String[] toArgs(String string) {
		return string.replaceAll("[ ]+", " ").split(" ");
	}

}
