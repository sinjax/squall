package org.openimaj.squall.functions.filters;

import org.openimaj.squall.compile.data.BaseContextIFunction;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class BaseFilterFunction extends BaseContextIFunction {

	@Override
	public boolean isStateless() {
		return true;
	}

	@Override
	public boolean forcedUnique() {
		return false;
	}

}
