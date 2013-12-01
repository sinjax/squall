package org.openimaj.storm.utils;

import java.util.List;
import java.util.Map;

import org.openimaj.kestrel.KestrelServerSpec;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KestrelParsedURI  {
	/**
	 * The list of kestrel hosts to try round robin
	 */
	public List<KestrelServerSpec> hosts;
	/**
	 * The queue addressed
	 */
	public String queue;
	/**
	 * The parameters after the "?"
	 */
	public Map<String, List<String>> params;
}
