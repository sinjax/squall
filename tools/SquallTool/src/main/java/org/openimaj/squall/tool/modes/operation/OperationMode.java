package org.openimaj.squall.tool.modes.operation;

import org.openimaj.squall.compile.data.IOperation;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface OperationMode {
	
	/**
	 * @return the oepration performed on the output
	 */
	public IOperation<Context> op();

}
