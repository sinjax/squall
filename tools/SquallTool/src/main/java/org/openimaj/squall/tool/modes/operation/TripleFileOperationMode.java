package org.openimaj.squall.tool.modes.operation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.kohsuke.args4j.Option;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TripleFileOperationMode implements OperationMode {

	/**
	 * the ketrel queues for input and output
	 */
	@Option(name = "--output-file", aliases = "-of", required = true, usage = "The file to output the resulting triples to", metaVar = "STRING")
	public String outFile = null;

	static final class FileOutputOperation implements IOperation<Context>,Serializable {

		private FileOutputStream fos;
		private File outFile;

		public FileOutputOperation(File outFile) {
			this.outFile = outFile;
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -1409401688854637882L;

		@Override
		public void setup() {
			try {
				this.fos = new FileOutputStream(outFile);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void cleanup() {
			try {
					this.fos.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void perform(Context object) {
			if (object.containsKey("triple")) {
				Graph graph = GraphFactory.createGraphMem();
				Triple typed = object.getTyped("triple");
				graph.add(typed);
				RDFDataMgr.write(fos, graph, Lang.NTRIPLES);
				try {
					this.fos.flush();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public IOperation<Context> op() {
		return new FileOutputOperation(new File(outFile));
	}

}
