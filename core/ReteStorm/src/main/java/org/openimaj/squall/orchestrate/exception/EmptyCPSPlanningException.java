package org.openimaj.squall.orchestrate.exception;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class EmptyCPSPlanningException extends PlanningException {

	/**
	 * 
	 */
	public EmptyCPSPlanningException(){
		super("CompiledProductionSystem produces no consequences.");
	}
	
	/**
	 * @param message
	 */
	public EmptyCPSPlanningException(String message){
		super("Empty CPS: "+message);
	}
	
	/**
	 * @param error
	 */
	public EmptyCPSPlanningException(Exception error){
		super("CompiledProductionSystem produces no consequences.", error);
	}
	
	/**
	 * @param message
	 * @param error
	 */
	public EmptyCPSPlanningException(String message, Exception error){
		super("Empty CPS: "+message, error);
	}
	
}
