package org.openimaj.squall.tool;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.bouncycastle.util.Strings;
import org.junit.Test;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.compile.data.jena.TripleFilterFunction;
import org.openimaj.squall.utils.JenaUtils;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.KestrelStream;

import com.esotericsoftware.kryo.Kryo;
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
	
	/**
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * 
	 */
	@Test
	public void testSquallToolKestrelQueue() throws IOException, URISyntaxException{
		URI op = new URI("kestrel://localhost/output");
		SquallTool.main(toArgs(""
			+ "-tm RIF "
				+ " -rifr java:///kestrelqueries.rif"
			+ " -pm GREEDYCS"
			+ " -bm STORM"
				+ " -stm LOCAL"
					+ " -st 5000" 
			+ " -o KESTREL"
				+ " -kh " + op.toString()
		));
		
		new KestrelStream<Context>(op,new MultiFunction<byte[], Context>() {

			private Kryo kryo = JenaStormUtils.kryo();

			@Override
			public List<Context> apply(byte[] in) {
				Context c = StormUtils.deserialiseFunction(kryo,in );
				return Arrays.asList(c);
			}
		}).forEach(new Operation<Context>() {
			
			@Override
			public void perform(Context object) {
				assertTrue(object!=null);
			}
		},8);
	}

	private String[] toArgs(String string) {
		return string.replaceAll("[ ]+", " ").split(" ");
	}

}
