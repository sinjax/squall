package org.openimaj.squall.build.storm.topology;

import org.openimaj.rdf.storm.utils.JenaStormUtils;
import org.openimaj.squall.compile.data.IOperation;

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
		public static final String SLEEPKEY = "org.openimaj.squall.build.storm.sleep";

		long DEFAULT_SLEEP_TIME = 5000;
		
		private Config conf;
		private String name = "local";

		private LocalCluster cluster;

		public LocalClusterOperation(Config conf) {
			this.conf = conf;
			this.conf.put(Config.STORM_LOCAL_MODE_ZMQ, true);
		}

		@Override
		public void perform(StormTopology object) {
			cluster.submitTopology(name, conf, object);
		}

		@Override
		public void setup() {
			this.cluster = new LocalCluster();
//			JenaStormUtils.registerSerializers(conf);
//			conf.setFallBackOnJavaSerialization(true);
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
				cluster.killTopology(name);
				cluster.shutdown();
			}
			
		}
		
	}