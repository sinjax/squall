package org.openimaj.squall.functions.rif.filters;

import org.openimaj.squall.compile.data.rif.AbstractRIFFunction;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class BaseFilterFunction extends AbstractRIFFunction {

	@Override
	public boolean isStateless() {
		return true;
	}

	@Override
	public boolean forcedUnique() {
		return false;
	}

}
