package org.openimaj.squall.compile;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Create a {@link CompiledProductionSystem}
 * @param <T> The type which the {@link Compiler} can compile from
 *
 */
public interface Compiler<T> {
	
	/**
	 * @param type 
	 * @return produce a {@link CompiledProductionSystem}
	 */
	public CompiledProductionSystem compile(T type);
}
