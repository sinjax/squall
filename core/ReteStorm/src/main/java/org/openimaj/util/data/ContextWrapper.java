package org.openimaj.util.data;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

import com.hp.hpl.jena.graph.Triple;

/**
 * Wrap a stream as a context. The stream content is put in the key
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ContextWrapper implements Function<Triple,Context>{
	
	private String key;

	/**
	 * @param key
	 */
	public ContextWrapper(String key) {
		this.key = key;
	}

	@Override
	public Context apply(Triple in) {
		Context ret = new Context();
		ret.put(key, in);
		return ret;
	}
	
}