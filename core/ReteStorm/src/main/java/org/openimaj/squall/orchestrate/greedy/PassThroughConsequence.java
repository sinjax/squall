package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PassThroughConsequence implements IFunction<Context, Context> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public List<Context> apply(Context in) {
		ArrayList<Context> ret = new ArrayList<Context>();
		ret.add(in);
		return ret;
	}

	@Override
	public void setup() {}
	@Override
	public void cleanup() {}

}
