package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.OverflowHandler;
import org.openimaj.rdf.storm.utils.OverflowHandler.CapacityOverflowHandler;
import org.openimaj.rdf.storm.utils.OverflowHandler.DurationOverflowHandler;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.SIVFunction;
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
public class StreamAwareFixedJoinFunction implements SIVFunction<Context, Context>, CapacityOverflowHandler<Map<String, Node>>, DurationOverflowHandler<Map<String,Node>> {
	private static final Logger logger = Logger.getLogger(FixedJoinFunction.class);
	private static final String DEFAULT_STREAM_SUFFIX= "";
	private static final String CAPACITY_OVERFLOW_STREAM_SUFFIX= "_capacity-overflow";
	private static final String DURATION_OVERFLOW_STREAM_SUFFIX= "_duration-overflow";
	
	private VariableHolder left;
	private VariableHolder right;
	private String leftStream;
	private String rightStream;
	private FixedHashSteM leftQueue;
	private FixedHashSteM rightQueue;
	private WindowInformation leftWI;
	private WindowInformation rightWI;
	private List<String> shared;
	private List<String> vars;
	
	private Set<String> streamNames;
	private Map<String, List<Context>> streamBuffers;
	private String defaultStream = DEFAULT_STREAM_SUFFIX;
	private String capacityOverflowStream = CAPACITY_OVERFLOW_STREAM_SUFFIX;
	private String durationOverflowStream = DURATION_OVERFLOW_STREAM_SUFFIX;
	
	/**
	 * @param left
	 * @param leftStream 
	 * @param leftwi 
	 * @param right
	 * @param rightStream 
	 * @param rightwi 
	 */
	public StreamAwareFixedJoinFunction(
			IVFunction<Context,Context> left,
			String leftStream,
			WindowInformation leftwi,
			IVFunction<Context,Context> right,
			String rightStream,
			WindowInformation rightwi
	) {
		
		this.left = left;
		this.right = right;
		
		this.leftStream = leftStream;
		this.rightStream = rightStream;
		
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
			if(allrightvar.contains(lv)) shared.add(lv);
		}
		this.leftWI = leftwi;
		this.rightWI = rightwi;
		
		this.streamNames = new HashSet<String>();
		this.streamNames.add(defaultStream);
		this.streamNames.add(capacityOverflowStream);
		this.streamNames.add(durationOverflowStream);
		this.streamBuffers = null;
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
	public Set<String> getOutputStreamNames() {
		Set<String> names = new HashSet<String>();
		names.addAll(this.streamNames);
		return names;
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
		for (int i = 0; i < this.shared.size(); i++){
			String newVar = varmap.get(this.shared.get(i));
			if (newVar != null) this.shared.set(i, newVar);
		}
		for (int i = 0; i < this.vars.size(); i++){
			String newVar = varmap.get(this.vars.get(i));
			if (newVar != null) this.vars.set(i, newVar);
		}
	}
	
	@Override
	public void mapInputStreams(Map<String, String> newNamesMap) {
		String newLeft = newNamesMap.get(leftStream);
		String newRight = newNamesMap.get(rightStream);
		if (newLeft != null) this.leftStream = newLeft;
		if (newRight != null) this.rightStream = newRight;
	}
	
	@Override
	public void mapOutputStreams(Map<String, String> newNamesMap) {
		Set<String> newStreamNames = new HashSet<String>();
		for (String name : this.streamNames){
			String newName = newNamesMap.get(name);
			if (name == null) newStreamNames.add(name);
			else newStreamNames.add(newName);
			
			if (name.equals(this.defaultStream)) this.defaultStream = newName;
			if (name.equals(this.capacityOverflowStream)) this.capacityOverflowStream = newName;
			if (name.equals(this.durationOverflowStream)) this.durationOverflowStream = newName;
		}
		this.streamNames = newStreamNames;
	}
	
	@Override
	public void setup() {
		leftQueue = new FixedHashSteM(this, shared,leftWI);
		rightQueue = new FixedHashSteM(this, shared,rightWI);
		
		this.streamBuffers = new HashMap<String, List<Context>>();
		for (String name : this.streamNames){
			this.streamBuffers.put(name, new ArrayList<Context>());
		}
	}

	@Override
	public void cleanup() {
		this.left = null;
		this.right = null;
		
		this.leftQueue = null;
		this.rightQueue = null;
		
		this.streamBuffers = null;
	}
	
	@Override
	public List<Context> apply(String stream, Context in) {
		Map<String, Node> typed = in.getTyped("bindings");
		logger.debug(String.format("Joining: %s with %s", this, typed));
		List<Context> ret = new ArrayList<Context>();
		if(stream.equals(this.leftStream)){
			if (leftQueue.build(typed)){
				logger.debug("Joining Left Stream");
				for (Map<String, Node> bindings : rightQueue.probe(typed)) {
					logger.debug(String.format("Joined: %s -> %s", typed, bindings));
					Context r = new Context();
					r.put("bindings",bindings);
					ret.add(r);
				}
			}
		}
		else if(stream.equals(this.rightStream)){
			if (rightQueue.build(typed)){
				logger.debug("Joining Right Stream");
				for (Map<String, Node> bindings : leftQueue.probe(typed)) {
					logger.debug(String.format("Joined: %s -> %s", typed, bindings));
					Context r = new Context();
					r.put("bindings",bindings);
					ret.add(r);
				}
			}
		}
		
		this.streamBuffers.get(this.defaultStream).addAll(ret);
		
		return ret;
	}
	
	@Override
	public void handleCapacityOverflow(Map<String,Node> overflowedBindings) {
		Context overflow = new Context();
		overflow.put("bindings", overflowedBindings);
		this.streamBuffers.get(this.capacityOverflowStream).add(overflow);
	}
	
	@Override
	public void handleDurationOverflow(Map<String,Node> overflowedBindings) {
		Context overflow = new Context();
		overflow.put("bindings", overflowedBindings);
		this.streamBuffers.get(this.durationOverflowStream).add(overflow);
	}

	@Override
	public List<Context> getStreamBuffer(String streamName) {
		List<Context> buffer = this.streamBuffers.get(streamName);
		if (buffer == null) return new ArrayList<Context>();
		this.streamBuffers.put(streamName, new ArrayList<Context>());
		return buffer;
	}
	
	@Override
	public String toString() {
		return String.format("JOIN: sharedVariables: %s, outputVariables: %s",this.shared.toString(),this.vars.toString());
	}
	
}