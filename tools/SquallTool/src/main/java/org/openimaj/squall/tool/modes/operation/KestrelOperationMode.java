package org.openimaj.squall.tool.modes.operation;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.util.data.Context;

public class KestrelOperationMode implements OperationMode {
	private final class KestrelOperation implements IOperation<Context> {
		
		private List<String> kestrelHosts;
		private String inputQueue;
		private String outputQueue;
		private List<KestrelServerSpec> kestrelSpecList;

		public KestrelOperation(List<String> kestrelHosts, String inputQueue,String outputQueue) {
			this.kestrelHosts = kestrelHosts;
			this.inputQueue = inputQueue;
			this.outputQueue = outputQueue;
		}

		@Override
		public void setup() {
			
		}
		
		private void prepareKestrelSpecList() {
			if (this.kestrelHosts.size() == 0) {
				this.kestrelHosts.add(String.format(KESTREL_FORMAT, KestrelServerSpec.LOCALHOST, KestrelServerSpec.DEFAULT_KESTREL_THRIFT_PORT));
			}
			this.kestrelSpecList = KestrelServerSpec.parseKestrelAddressList(this.kestrelHosts);
		}

		@Override
		public void cleanup() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void perform(Context object) {
			// TODO Auto-generated method stub
			
		}
	}


	private static final String KESTREL_FORMAT = "%s:%s";
	
	/**
	 * the ketrel queues for input and output
	 */
	@Option(
			name = "--kestrel-host",
			aliases = "-kh",
			required = false,
			usage = "The message queue host from which and to which triples will be written",
			metaVar = "STRING",
			multiValued = true)
	public List<String> kestrelHosts = new ArrayList<String>();
	
	/**
	 * the input queue from which triples are read by the pipeline
	 */
	@Option(
			name = "--kestrel-input-queue",
			aliases = "-kiq",
			required = false,
			usage = "The input queue")
	public String inputQueue = "inputQueue";

	/**
	 *
	 */
	@Option(
			name = "--kestrel-output-queue",
			aliases = "-koq",
			required = false,
			usage = "The output queue")
	public String outputQueue = "outputQueue";

	private List<KestrelServerSpec> kestrelSpecList;
	

	@Override
	public IOperation<Context> op() {
		return new KestrelOperation(this.kestrelHosts,inputQueue,outputQueue);
	}

}
