package org.openimaj.squall.orchestrate.exception;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class PlanningException extends Exception {
	
	/**
	 * 
	 */
	public PlanningException(){
		super("Planner Exception.");
	}
	
	/**
	 * @param message
	 */
	public PlanningException(String message){
		super("Planner Exception: "+message);
	}
	
	/**
	 * @param error
	 */
	public PlanningException(Exception error){
		super("Planning Exception.", error);
	}
	
	/**
	 * @param message
	 * @param error
	 */
	public PlanningException(String message, Exception error){
		super("Planning Exception: "+message, error);
	}

}
