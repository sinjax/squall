package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PassThroughConsequence implements IVFunction<Context, Context> {

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
	public List<String> variables() {
		return new ArrayList<String>();
	}

	@Override
	public String anonimised(Map<String, Integer> varmap) {
		return anonimised();
	}

	@Override
	public String anonimised() {
		return anonimised();
	}

	@Override
	public void mapVariables(Map<String, String> varmap) {
		// do nothing
	}

	@Override
	public void setup() {
		// do nothing
	}

	@Override
	public void cleanup() {
		// do nothing
	}

	

}
