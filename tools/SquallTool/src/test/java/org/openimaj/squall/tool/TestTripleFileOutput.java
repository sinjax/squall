package org.openimaj.squall.tool;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.riot.Lang;
import org.junit.Test;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.squall.tool.modes.operation.TripleFileOperationMode;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestTripleFileOutput {
	
	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testWriteTriple() throws IOException{
		Iterator<Triple> iter = JenaUtils.createIterator(TestTripleFileOutput.class.getResourceAsStream("/test.rdfs"), Lang.NTRIPLES);
		
		TripleFileOperationMode tfom = new TripleFileOperationMode();
		File tmp = File.createTempFile("out", ".ttl");
		
		tfom.outFile = tmp.getAbsolutePath();
		IOperation<Context> op = tfom.op();
		op.setup();
		while(iter.hasNext()){
			op.perform(new Context("triple",iter.next()));
		}
		op.cleanup();
		iter = JenaUtils.createIterator(new FileInputStream(tmp), Lang.NTRIPLES);
		while(iter.hasNext()){
			System.out.println(iter.next());
		}
		tmp.deleteOnExit();
		tmp.delete();
		assertTrue(!tmp.exists());
	}
}
