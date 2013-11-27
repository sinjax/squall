package org.openimaj.squall.tool.modes.operation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WriterDatasetRIOT;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TripleFileOperationMode implements OperationMode {

	/**
	 * the ketrel queues for input and output
	 */
	@Option(
			name = "--output-file",
			aliases = "-of",
			required = true,
			usage = "The file to output the resulting triples to",
			metaVar = "STRING")
	public String outFile = null;
	
	private final class FileOutputOperation implements IOperation<Context>,Serializable {
		
		private PrintWriter writer;
		private WriterDatasetRIOT twriter;
		public FileOutputOperation() {
			twriter = RDFDataMgr.createDatasetWriter(Lang.NTRIPLES);
			
			try {
				this.writer = new PrintWriter(
						new FileOutputStream(
								new File(outFile)
								)
						);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = -1409401688854637882L;
		@Override
		public void setup() {
			System.out.println("Starting Test");
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void perform(Context object) {
			if(object.containsKey("triple"))
			{
				Graph graph = GraphFactory.createGraphMem();
				Triple typed = object.getTyped("triple");
				graph.add(typed);
				twriter.write(writer, graph, null, null, null);
				
			}
		}
	}
	@Override
	public IOperation<Context> op() {
		return new FileOutputOperation();
	}

}
