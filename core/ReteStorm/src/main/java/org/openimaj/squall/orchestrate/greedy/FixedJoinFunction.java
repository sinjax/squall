package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private VariableHolder left;
	private VariableHolder right;
	private ArrayList<String> shared;
	private MapRETEQueue leftQueue;
	private MapRETEQueue rightQueue;
	private ArrayList<String> vars;
	
	/**
	 * @param left
	 * @param right
	 */
	public FixedJoinFunction(
			IVFunction<Context,Context> left,
			IVFunction<Context,Context> right
	) {
		this.left = left;
		this.right = right;
		
		// Find the join variables 
		List<String> allleftvar = left.variables();
		List<String> allrightvar = right.variables();
		
		Set<String> allvarset = new HashSet<>();
		allvarset.addAll(allleftvar);
		allvarset.addAll(allrightvar);
		
		this.vars = new ArrayList<String>();
		this.vars.addAll(allvarset);
		// The shared variables must match
		shared = new ArrayList<String>();
		for (String lv : allleftvar) {
			shared.add(lv);
		}
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String, Node> typed = in.getTyped("bindings");
		List<Context> ret = new ArrayList<Context>();
		if(in.getTyped("stream").equals("left")){
			
			for (Map<String, Node> bindings : leftQueue.offer(typed)) {
				ret.add(new Context("bindings",bindings));
			}
		}
		else if(in.getTyped("stream").equals("right")){
			for (Map<String, Node> bindings : rightQueue.offer(typed)) {
				ret.add(new Context("bindings",bindings));
			}
		}
		return ret;
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
	public void setup() {
		leftQueue = new MapRETEQueue(shared);
		rightQueue = new MapRETEQueue(shared);
		
		leftQueue.pair(rightQueue);
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public void mapVariables(Map<String, String> varmap) {
		// TODO Implement Variable Mapping
		
	}
	
}