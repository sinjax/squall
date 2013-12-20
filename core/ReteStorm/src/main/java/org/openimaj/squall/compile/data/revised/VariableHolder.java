package org.openimaj.squall.compile.data.revised;

import java.util.List;
import org.openimaj.util.function.Function;

/**
 * Allows access to the variables utilised by the {@link VariableHolder}, both the underlying variables used during processing,
 * and their equivalent variables in last rule to make use of it during orchestration.
 * Also provide a method to access an anonimised representation of the {@Link VariableHolder} (e.g. present an underlying {@link Function} as
 * an implementation and rule independent string), as well as a method to access the list of contributing atomic {@link VariableHolder}s.
 */
public interface VariableHolder{
	/**
	 * @return the underlying variables used in this function, ordered by appearance in the function output.
	 */
	public List<String> variables();
	
}
