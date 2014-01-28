package org.openimaj.squall.build.storm.topology;

import org.openimaj.squall.compile.data.IOperation;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.utils.Utils;

/**
	 * A local cluster
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public class LocalClusterOperation implements IOperation<StormTopology>{
		
		private static final String NAME = "local";
		/**
		 * 
		 */
		public static final String SLEEPKEY = "org.openimaj.squall.build.storm.sleep";

		/**
		 * 
		 */
		public static final long DEFAULT_SLEEP_TIME = 5000;
		
		private Config conf;

		private LocalCluster cluster;

		/**
		 * @param conf
		 */
		public LocalClusterOperation(Config conf) {
			this.conf = conf;
			this.conf.put(Config.STORM_LOCAL_MODE_ZMQ, true);
		}

		@Override
		public void perform(StormTopology object) {
			cluster.submitTopology(NAME, conf, object);
		}

		@Override
		public void setup() {
			this.cluster = new LocalCluster();
		}

		@Override
		public void cleanup() {
			long sleepTime = (Long) conf.get(SLEEPKEY);
			try {
				if (sleepTime < 0) {
					while (true) {
						Utils.sleep(DEFAULT_SLEEP_TIME);
					}
				} else {
					Utils.sleep(sleepTime);

				}
			} finally {
				cluster.killTopology(NAME);
				cluster.shutdown();
			}
			
		}

		@Override
		public void write(Kryo kryo, Output output) {
			kryo.writeClassAndObject(output, this.conf);
		}

		@Override
		public void read(Kryo kryo, Input input) {
			this.conf = (Config) kryo.readClassAndObject(input);
		}

		@Override
		public boolean isStateless() {
			return true;
		}

		@Override
		public boolean forcedUnique() {
			return true;
		}
		
	}