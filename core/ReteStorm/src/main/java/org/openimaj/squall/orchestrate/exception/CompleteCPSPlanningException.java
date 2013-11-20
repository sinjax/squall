package org.openimaj.squall.orchestrate.exception;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class CompleteCPSPlanningException extends PlanningException {

	/**
	 * 
	 */
	public CompleteCPSPlanningException(){
		super("CompiledProductionSystem has been fully processed and cannot be modified.");
	}
	
	/**
	 * @param message
	 */
	public CompleteCPSPlanningException(String message){
		super("Complete CPS: "+message);
	}
	
	/**
	 * @param error
	 */
	public CompleteCPSPlanningException(Exception error){
		super("CompiledProductionSystem has been fully processed and cannot be modified.", error);
	}
	
	/**
	 * @param message
	 * @param error
	 */
	public CompleteCPSPlanningException(String message, Exception error){
		super("Complete CPS: "+message, error);
	}
	
}
