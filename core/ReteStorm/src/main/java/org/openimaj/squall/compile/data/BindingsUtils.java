package org.openimaj.squall.compile.data;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class BindingsUtils {
	
	/**
	 * @param map
	 * @param ruleVars
	 * @return
	 * 		an array of the Jena Nodes that are the bindings of the desired rule variables, in the order defined by the ruleVars array.
	 */
	public static Node[] mapToArray(Map<String,Node> map, String[] ruleVars){
		Node[] vals = new Node[ruleVars.length];
		for (int i = 0; i < ruleVars.length; i++)
			vals[i] = map.get(ruleVars[i]);
		return vals;
	}
	
	/**
	 * @param array
	 * @param ruleVars
	 * @return
	 * 		A Map<String,Node> of Strings to Jena Nodes representing the values of the ruleVariables bound to their variable names.
	 */
	public static Map<String,Node> arrayToMap(Node[] array, String[] ruleVars){
		Map<String,Node> binding = new HashMap<String,Node>();
		for (int i = 0; i < ruleVars.length; i++)
			binding.put(ruleVars[i], array[i]);
		return binding;
	}

	/**
	 * @param clause
	 * @param bindings
	 * @return A triple instansiation of the {@link TriplePattern} with its variables filled in from the bindings map
	 */
	public static Triple instantiate(TriplePattern clause,Map<String, Node> bindings) {
		
		Node s = extractVariable(bindings, clause.getSubject());
		Node p = extractVariable(bindings, clause.getPredicate());
		Node o = extractVariable(bindings, clause.getObject());
		
		return new Triple(s, p, o);
	}
	
	/**
	 * @param clause
	 * @param in 
	 * @return The bindings produced by mapping a {@link Triple} onto a {@link TriplePattern}.  null if the in {@link Triple} does not match the clause {@link TriplePattern}.  If in is null, returns null because null does not match any pattern.
	 */
	public static Map<String,Node> extractVars(TriplePattern clause, Triple in) {
		
		if (in == null) return null;
		
		// Create a Map of Variable Strings to Nodes 
		Map<String,Node> binds = new HashMap<String,Node>();

		// For each part of the triple, check if the Pattern declares it to be variable
		// (or a functor, in the case of Objects)
		if (clause.getSubject().isVariable()){
			// if it is a variable, insert its value into the array of Values
			binds.put(clause.getSubject().getName(),in.getSubject());
		} else if ( ! clause.getSubject().sameValueAs(in.getSubject()))
		{
			return null;
		}

		if (clause.getPredicate().isVariable()){
			// For each subsequent variable, check that the variable has not already been
			// seen within this triple.
			if (binds.containsKey(clause.getPredicate().getName())){
				// If it has and the values are different, then the Triple is not a match, so
				// do not fire, and move onto the next Triple.
				if ( ! in.getPredicate().sameValueAs( binds.get(clause.getPredicate().getName()) ));
				{
					return null;
				}
			} else {
				// If the variable has not been seen before, process the node as with the Subject.
				binds.put(clause.getPredicate().getName(),in.getPredicate());
			}
		} else if ( ! clause.getPredicate().sameValueAs(in.getPredicate()))
		{
			return null;
		}

		if (clause.getObject().isVariable()){
			if (binds.containsKey(clause.getObject().getName())) {
				if ( ! in.getObject().sameValueAs( binds.get( clause.getObject().getName() ) )  )
				{
					return null;
				}
			} else {
				binds.put(clause.getObject().getName(),in.getObject());
			}
		} else if (clause.getObject().isLiteral() && clause.getObject().getLiteralValue() instanceof Functor){
			// if the object is a functor, check each node in the functor to see if it is a variable,
			// and treat each as if it were a more traditional part of the Triple.
			Functor f = (Functor)clause.getObject().getLiteralValue();
			Functor functor;
			if (in.getObject().isLiteral()
					&& in.getObject().getLiteralValue() instanceof Functor
					&& (functor = (Functor)in.getObject().getLiteralValue()).getArgLength() == f.getArgLength()) {
				for (int i = 0; i < f.getArgs().length; i++){
					Node n = f.getArgs()[i];
					if (n.isVariable())
						if (binds.containsKey(n.getName())){
							if ( ! functor.getArgs()[i].sameValueAs( binds.get( n.getName() ) ) )
							{
								return null;
							}
						} else {
							binds.put(n.getName(), functor.getArgs()[i]);
						}
					else
					{
						if ( ! n.sameValueAs(functor.getArgs()[i]))
						{
							return null;
						}
					}
				}
			} else
			{
				return null;
			}
		} else if ( ! clause.getObject().sameValueAs(in.getObject())){
			return null;
		}
		return binds;
	}
	
	/**
	 * @param clause
	 * @param bindings
	 * @return A {@link Functor} instansiation of the {@link Functor} pattern with its variables filled in from the bindings map
	 */
	public static Functor instantiate(Functor clause,Map<String, Node> bindings) {
		
		Node[] args = new Node[clause.getArgLength()];
		Node[] params = clause.getArgs();
		
		for (int i = 0; i < args.length; i++){
			args[i] = extractVariable(bindings, params[i]);
		}
		
		return new Functor(clause.getName(), args);
	}
	
	/**
	 * @param clause
	 * 		The {@link Functor} pattern, which may contain variables as arguments.
	 * @param in 
	 * 		The ground {@link Functor} to match against the clause.
	 * @return The bindings produced by mapping a {@link Functor} onto a {@link Functor} with variables as arguments.  null if the in {@link Functor} does not match the clause {@link Functor}.  If in is null, returns null because null does not match any pattern.
	 */
	public static Map<String, Node> extractVars(Functor clause,Functor in) {
		if (in == null || !clause.getName().equals(in.getName()) || clause.getArgLength() != in.getArgLength()) return null;
		
		Map<String, Node> binds = new HashMap<String, Node>();
		
		Node[] clauseArgs = clause.getArgs();
		Node[] inArgs = in.getArgs();
		for (int i = 0; i < clause.getArgLength(); i++)
			if (!inArgs[i].sameValueAs(clauseArgs[i]))
				if (clauseArgs[i].isVariable())
					binds.put(clauseArgs[i].getName(), inArgs[i]);
				else
					return null;
		
		return binds;
	}

	private static Node extractVariable(Map<String, Node> bindings, Node subject) {
		Node s;
		if(subject.isVariable()){
			s = bindings.get(subject.getName());
			if(s == null){
				s = NodeFactory.createAnon();
			}
		}
		else{
			s = subject;
		}
		return s;
	}  
	
}
