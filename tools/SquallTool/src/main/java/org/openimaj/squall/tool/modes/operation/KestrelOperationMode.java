package org.openimaj.squall.tool.modes.operation;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift7.TException;
import org.kohsuke.args4j.Option;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.storm.utils.KestrelUtils;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.data.Context;

import backtype.storm.spout.KestrelThriftClient;

import com.esotericsoftware.kryo.Kryo;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelOperationMode implements OperationMode {
	protected static final Kryo kryo;
	static{
		kryo = new Kryo();
		JenaStormUtils.registerSerializers(kryo);
	}
	private static final Logger logger = Logger.getLogger(KestrelOperationMode.class);
	private final class KestrelOperation implements IOperation<Context> {
		private List<String> kestrelHosts;
		private String outputQueue;
		private ArrayList<KestrelThriftClient> clients;
		private ArrayList<String> kestrelServers;
		private int port;
		private int clientIndex = 0;
		private boolean forceDelete;

		public KestrelOperation(List<String> kestrelHosts, String outputQueue, boolean forceDelete) {
			this.kestrelHosts = kestrelHosts;
			this.outputQueue = outputQueue;
			this.kestrelServers = new ArrayList<String>();
			this.port = 0;
			this.forceDelete = forceDelete;
			prepareKestrelSpecList();
			
		}

		@Override
		public void setup() {
			clients = new ArrayList<KestrelThriftClient>();
			for (String server : kestrelServers) {
				try {
					KestrelThriftClient client = new KestrelThriftClient(server, port);
					this.clients.add(client);
				} catch (TException e) {
					logger.error("Failed to create Kestrel client for host: " + server);
					throw new RuntimeException(e);
				}
			}
		}
		
		private void prepareKestrelSpecList() {
			if (this.kestrelHosts.size() == 0) {
				this.kestrelHosts.add(String.format(KESTREL_FORMAT, KestrelServerSpec.LOCALHOST, KestrelServerSpec.DEFAULT_KESTREL_THRIFT_PORT));
			}
			List<KestrelServerSpec> kestrelSpecList = KestrelServerSpec.parseKestrelAddressList(this.kestrelHosts);
			for (KestrelServerSpec kestrelServerSpec : kestrelSpecList) {
				this.kestrelServers.add(kestrelServerSpec.host);
				this.port = kestrelServerSpec.port;
				if(forceDelete){
					try {
						KestrelUtils.deleteQueues(kestrelServerSpec, outputQueue);
					} catch (TException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		@Override
		public void cleanup() {
			for (KestrelThriftClient client : this.clients)
			{
				client.close();
			}
		}

		@Override
		public void perform(Context object) {
			KestrelThriftClient client = nextClient();
			byte[] vals = StormUtils.serialiseFunction(kryo,object);
			try {
				client.put(this.outputQueue, Arrays.asList(ByteBuffer.wrap(vals)), 10000);
			} catch (TException e) {
				
			}
		}

		private KestrelThriftClient nextClient() {
			KestrelThriftClient kestrelThriftClient = this.clients.get(clientIndex );
			clientIndex++;
			if (clientIndex == clients.size())
				clientIndex = 0;
			return kestrelThriftClient;
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
	 *
	 */
	@Option(
			name = "--kestrel-output-queue",
			aliases = "-koq",
			required = false,
			usage = "The output queue")
	public String outputQueue = "outputQueue";
	
	/**
	 *
	 */
	@Option(
			name = "--kestrel-delete-output",
			aliases = "-kdo",
			required = false,
			usage = "Delete the output queue")
	public boolean forceDelete = false;
	

	@Override
	public IOperation<Context> op() {
		return new KestrelOperation(this.kestrelHosts,outputQueue,forceDelete);
	}

}
