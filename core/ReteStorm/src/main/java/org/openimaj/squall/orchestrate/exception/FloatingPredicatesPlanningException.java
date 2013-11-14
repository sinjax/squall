package org.openimaj.squall.orchestrate.exception;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class FloatingPredicatesPlanningException extends PlanningException {
	
	/**
	 * 
	 */
	public FloatingPredicatesPlanningException(){
		super("Floating Predicates.");
	}
	
	/**
	 * @param message
	 */
	public FloatingPredicatesPlanningException(String message){
		super("Floating Predicates: "+message);
	}
	
	/**
	 * @param error
	 */
	public FloatingPredicatesPlanningException(Exception error){
		super("Floating Predicates.", error);
	}
	
	/**
	 * @param message
	 * @param error
	 */
	public FloatingPredicatesPlanningException(String message, Exception error){
		super("Floating Predicates: "+message, error);
	}
	
}
