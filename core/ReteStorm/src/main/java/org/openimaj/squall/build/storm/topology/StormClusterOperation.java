package org.openimaj.squall.build.storm.topology;

import org.openimaj.squall.compile.data.IOperation;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StormClusterOperation implements IOperation<StormTopology> {

	/**
	 * key used to extract topology names from configs
	 */
	public static final String TOPOLOGY_NAME_KEY = "org.openimaj.squall.topology.name";
	private Config conf;

	/**
	 * @param conf
	 */
	public StormClusterOperation(Config conf) {
		this.conf = conf;
	}

	@Override
	public void perform(StormTopology object) {
		try {
			StormSubmitter.submitTopology((String) conf.get(TOPOLOGY_NAME_KEY), conf, object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setup() {
	}

	@Override
	public void cleanup() {
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
