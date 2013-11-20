package org.openimaj.squall.orchestrate.exception;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class IncompleteCPSPlanningException extends PlanningException {

	/**
	 * 
	 */
	public IncompleteCPSPlanningException(){
		super("CompiledProductionSystem produces no explicit consequences.");
	}
	
	/**
	 * @param message
	 */
	public IncompleteCPSPlanningException(String message){
		super("Incomplete CPS: "+message);
	}
	
	/**
	 * @param error
	 */
	public IncompleteCPSPlanningException(Exception error){
		super("CompiledProductionSystem produces no explicit consequences.", error);
	}
	
	/**
	 * @param message
	 * @param error
	 */
	public IncompleteCPSPlanningException(String message, Exception error){
		super("Incomplete CPS: "+message, error);
	}
	
}
