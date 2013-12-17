package org.openimaj.squall.compile.data;

import java.util.List;
import java.util.Map;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <IN>
 * @param <OUT>
 */
public interface MultiInputStreamFunction<IN, OUT> {

	/**
	 * @param stream
	 * @param in
	 * @return
	 */
	List<OUT> apply(String stream, IN in);
	
	/**
	 * @param newNameMap
	 */
	void mapInputStreams(Map<String, String> newNameMap);
	
}
