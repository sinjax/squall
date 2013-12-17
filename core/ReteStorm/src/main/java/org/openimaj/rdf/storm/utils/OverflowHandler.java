package org.openimaj.rdf.storm.utils;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 * @param <E>
 */
public interface OverflowHandler<E> {
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 * @param <T>
	 */
	public static interface CapacityOverflowHandler<T> extends OverflowHandler<T> {
		
		/**
		 * @param overflow
		 */
		public void handleCapacityOverflow(T overflow);
		
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 * @param <T>
	 */
	public static interface DurationOverflowHandler<T> extends OverflowHandler<T> {
		
		/**
		 * @param overflow
		 */
		public void handleDurationOverflow(T overflow);
		
	}
	
}
