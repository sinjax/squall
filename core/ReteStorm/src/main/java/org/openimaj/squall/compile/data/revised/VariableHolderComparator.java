package org.openimaj.squall.compile.data.revised;

import java.util.Comparator;

/**
 * A comparator which can order {@link VariableHolder} instances 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class VariableHolderComparator implements Comparator<VariableHolder>{

	@Override
	public int compare(VariableHolder o1, VariableHolder o2) {
		return o1.anonimised().compareTo(o2.anonimised());
	}

}
