package org.openimaj.squall.compile.data;

import org.openimaj.util.data.Context;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class BaseContextIFunction implements IFunction<Context, Context> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7761395895169220137L;
	
	@Override
	public void setup() {}
	
	@Override
	public void cleanup() {}

}
