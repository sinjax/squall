package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.VariableHolder;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * A Fixed join function expects specific variables to come from one
 * stream and other specific variables to come from another.
 * 
 * This is not a suitable join function for node sharing
 * 
 */
public class FixedJoinFunction implements IVFunction<Context, Context>{
	private static final Logger logger = Logger.getLogger(FixedJoinFunction.class);
	private VariableHolder left;
	private VariableHolder right;
	private List<String> shared;
	private MapRETEQueue leftQueue;
	private MapRETEQueue rightQueue;
	private List<String> vars;
	private WindowInformation wi;
	
	/**
	 * @param left
	 * @param right
	 * @param wi 
	 */
	public FixedJoinFunction(
			IVFunction<Context,Context> left,
			IVFunction<Context,Context> right,
			WindowInformation wi
	) {
		
		this.left = left;
		this.right = right;
		
		// Find the join variables 
		Collection<String> allleftvar = left.variables();
		Collection<String> allrightvar = right.variables();
		
		Set<String> allvarset = new HashSet<>();
		allvarset.addAll(allleftvar);
		allvarset.addAll(allrightvar);
		
		this.vars = new ArrayList<String>();
		this.vars.addAll(allvarset);
		// The shared variables must match
		shared = new ArrayList<String>();
		for (String lv : allleftvar) {
			if(allrightvar.contains(lv)) shared.add(lv);
		}
		this.wi = wi;
	}
	
	@Override
	public List<String> variables() {
		return this.vars;
	}
	
	/**
	 * @return the shared variables of this join
	 */
	public List<String> sharedVars() {
		return this.shared;
	}

	@Override
	public String anonimised(Map<String, Integer> varmap) {
		return left.anonimised(varmap) + " " + right.anonimised(varmap);
	}

	@Override
	public String anonimised() {
		return left.anonimised() + " " + right.anonimised();
	}

	@Override
	public void mapVariables(Map<String, String> varmap) {
		// TODO Implement Variable Mapping
		
	}
	
	@Override
	public void setup() {
		leftQueue = new MapRETEQueue(shared,wi);
		rightQueue = new MapRETEQueue(shared,wi);
		
		leftQueue.pair(rightQueue);
	}

	@Override
	public void cleanup() {
		this.left = null;
		this.right = null;
		
		this.leftQueue = null;
		this.rightQueue = null;
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String, Node> typed = in.getTyped("bindings");
		logger.debug(String.format("Joining: %s with %s", this, typed));
		List<Context> ret = new ArrayList<Context>();
		if(in.getTyped("stream").equals("left")){
			logger.debug("Joining Left Stream");
			for (Map<String, Node> bindings : leftQueue.offer(typed)) {
				logger.debug(String.format("Joined: %s -> %s", typed, bindings));
				ret.add(new Context("bindings",bindings));
			}
		}
		else if(in.getTyped("stream").equals("right")){
			logger.debug("Joining Right Stream");
			for (Map<String, Node> bindings : rightQueue.offer(typed)) {
				logger.debug(String.format("Joined: %s -> %s", typed, bindings));
				ret.add(new Context("bindings",bindings));
			}
		}
		return ret;
	}
	
	@Override
	public String toString() {
		return String.format("JOIN: sharedVariables: %s, outputVariables: %s",this.shared.toString(),this.vars.toString());
	}
	
}