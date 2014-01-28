package org.openimaj.squall.tool.modes.operation;

import java.net.URISyntaxException;

import org.kohsuke.args4j.Option;
import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.storm.utils.StormUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.KestrelWriter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelOperationMode implements OperationMode {
	
	private final class KestrelOperation implements IOperation<Context> {
		private KestrelWriter writer;
		private Kryo kryo;

		public KestrelOperation(String hosts) throws URISyntaxException {
			this.writer = new KestrelWriter(hosts);
			
		}

		@Override
		public void setup() {
			this.writer.setup();
			kryo = JenaStormUtils.kryo();
		}
		@Override
		public void cleanup() {
			this.writer.cleanup();
		}

		@Override
		public void perform(Context object) {
			byte[] vals = StormUtils.serialiseFunction(kryo,object);
			this.writer.perform(vals);
		}

		@Override
		public void write(Kryo kryo, Output output) {
			kryo.writeClassAndObject(output, this.writer);
		}

		@Override
		public void read(Kryo kryo, Input input) {
			this.writer = (KestrelWriter) kryo.readClassAndObject(input);
		}

		@Override
		public boolean isStateless() {
			return true;
		}

		@Override
		public boolean forcedUnique() {
			return false;
		}
	}
	
	/**
	 * the ketrel queues for input and output
	 */
	@Option(
			name = "--kestrel-host",
			aliases = "-kh",
			required = false,
			usage = "The message queue host from which and to which triples will be written",
			metaVar = "STRING")
	public String kestrelHosts = "kestrel://localhost/output";
	
	@Override
	public IOperation<Context> op() {
		try {
			return new KestrelOperation(this.kestrelHosts);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
