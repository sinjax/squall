package org.openimaj.squall.orchestrate.exception;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class MultiConsequenceSubCPSPlanningException extends PlanningException {

	/**
	 * 
	 */
	public MultiConsequenceSubCPSPlanningException(){
		super("Multiple Consequences for joined sub-CPS.");
	}
	
	/**
	 * @param message
	 */
	public MultiConsequenceSubCPSPlanningException(String message){
		super("Multiple Consequences for joined sub-CPS: "+message);
	}
	
	/**
	 * @param error
	 */
	public MultiConsequenceSubCPSPlanningException(Exception error){
		super("Multiple Consequences for joined sub-CPS.", error);
	}
	
	/**
	 * @param message
	 * @param error
	 */
	public MultiConsequenceSubCPSPlanningException(String message, Exception error){
		super("Multiple Consequences for joined sub-CPS: "+message, error);
	}
	
}
