package org.openimaj.squall.compile.data;

import java.util.Comparator;

/**
 * A comparator which can order {@link VariableHolder} instances 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class VariableHolderComparator implements Comparator<AnonimisedRuleVariableHolder>{

	@Override
	public int compare(AnonimisedRuleVariableHolder o1, AnonimisedRuleVariableHolder o2) {
		return o1.identifier().compareTo(o2.identifier());
	}

}
