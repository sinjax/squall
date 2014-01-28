package org.openimaj.squall.functions.rif.consequences;

import org.openimaj.squall.compile.data.IConsequence;
import org.openimaj.squall.compile.data.rif.AbstractRIFFunction;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class BaseConsequenceFunction extends AbstractRIFFunction
		implements IConsequence {

	@Override
	public boolean isStateless() {
		return true;
	}

	@Override
	public boolean forcedUnique() {
		return false;
	}

}
