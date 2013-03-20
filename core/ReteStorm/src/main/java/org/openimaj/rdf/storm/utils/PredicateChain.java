package org.openimaj.rdf.storm.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * 
 */
public class PredicateChain {
	
	private final Node start;
	private final Node end;
	
	private final Triple newT;
	
	private final PredicateChain builtOn;
	
	/**
	 * Creates a new chain with the founding {@link Triple}, n. 
	 * @param n - The {@link Triple} forming the foundation of a chain.
	 */
	public PredicateChain(Triple n){
		this.start = n.getSubject();
		this.end = n.getObject();
		this.newT = n;
		this.builtOn = null;
	}
	
	private PredicateChain(PredicateChain b, Triple n){
		this.start = b.getStart();
		this.end = n.getObject();
		this.newT = n;
		this.builtOn = b;
	}
	
	private PredicateChain(Triple n, PredicateChain b){
		this.start = n.getSubject();;
		this.end = b.getEnd();
		this.newT = n;
		this.builtOn = b;
	}
	
	private boolean checkNoLoopToSubject(Node sub){
		return !sub.sameValueAs(this.newT.getObject()) && this.builtOn == null ? true : this.builtOn.checkNoLoopToSubject(sub);
	}
	
	private boolean checkNoLoopToObject(Node obj){
		return !obj.sameValueAs(this.newT.getSubject()) && this.builtOn == null ? true : this.builtOn.checkNoLoopToObject(obj);
	}
	
	/**
	 * @return The {@link Node} that is the Subject of the first {@link Triple} in the chain.
	 */
	public Node getStart(){
		return start;
	}
	
	/**
	 * @return The {@link Node} that is the Object of the first {@link Triple} in the chain.
	 */
	public Node getEnd(){
		return end;
	}
	
	/**
	 * Constructs a new chain extending the existing {@link PredicateChain} with the new {@link Triple} IFF:
	 * <ul>
	 * 	<li>the {@link Triple} extends the chain</li>
	 * 	AND
	 * 	<li>the {@link Triple} does not create a loop in the chain.</li>
	 * </ul>
	 * @param n - The {@link Triple} extending the existing {@link PredicateChain}
	 * @return An extended {@link PredicateChain}
	 * @throws IncompatibleChainException - When the new {@link Triple} violates the above clauses.
	 */
	public PredicateChain extendChain(Triple n) throws IncompatibleChainException{
		if (n.getSubject().sameValueAs(this.getEnd())){
			if (this.checkNoLoopToObject(n.getObject())){
				return new PredicateChain(this,n);
			}
		} else if (n.getObject().sameValueAs(this.getStart())){
			if (this.checkNoLoopToSubject(n.getSubject())){
				return new PredicateChain(n,this);
			}
		}
		throw new IncompatibleChainException();
	}
	
	private static PredicateChain extendChainWithoutLoops(PredicateChain output, PredicateChain start, PredicateChain end) throws IncompatibleChainException{
		if (start.checkNoLoopToObject(end.newT.getObject())){
			try{
				if (start.getEnd().sameValueAs(end.builtOn.getStart())){	
					return new PredicateChain(extendChainWithoutLoops(output,
																			 start,
																			 end.builtOn),
											  end.newT);
				} else {
					return extendChainWithoutLoops(new PredicateChain(output,
																			 end.newT),
														  start,
														  end.builtOn);
				}
			} catch (NullPointerException e) {
				return extendChainWithoutLoops(new PredicateChain(output,
																		 end.newT),
													  start,
													  end.builtOn);
			}
		}
		throw new IncompatibleChainException();
	}
	
	/**
	 * Constructs a new chain extending the existing {@link PredicateChain} with the new {@link PredicateChain} IFF:
	 * <ul>
	 * 	<li>the new {@link PredicateChain} extends the existing {@link PredicateChain}</li>
	 * 	AND
	 * 	<li>the new {@link PredicateChain} does not create a loop in the chain.</li>
	 * </ul>
	 * @param other - The new {@link PredicateChain} extending the existing {@link PredicateChain}
	 * @return An extended {@link PredicateChain}
	 * @throws IncompatibleChainException - When the new {@link PredicateChain} violates the above clauses.
	 */
	public PredicateChain extendChain(PredicateChain other) throws IncompatibleChainException{
		if (other.getStart().sameValueAs(this.getEnd())){
			return extendChainWithoutLoops(this,this,other);
		} else if (this.getStart().sameValueAs(other.getEnd())){
			return extendChainWithoutLoops(other,other,this);
		}
		throw new IncompatibleChainException();
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 * 
	 */
	public static class IncompatibleChainException extends Exception{private static final long serialVersionUID = 513027440018260601L;}
	
}
