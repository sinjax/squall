package org.openimaj.squall.revised.orchestrate.rete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.OverflowHandler.CapacityOverflowHandler;
import org.openimaj.rdf.storm.utils.OverflowHandler.DurationOverflowHandler;
import org.openimaj.squall.orchestrate.WindowInformation;
import org.openimaj.squall.orchestrate.greedy.FixedHashSteM;
import org.openimaj.squall.revised.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.revised.compile.data.IVFunction;
import org.openimaj.squall.revised.compile.data.SIVFunction;
import org.openimaj.squall.revised.compile.data.VariableHolderAnonimisationUtils;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Node;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 * 
 * A Fixed join function expects specific variables to come from one
 * stream and other specific variables to come from another.
 * 
 * This is not a suitable join function for node sharing
 * 
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class StreamAwareFixedJoinFunction implements SIVFunction<Context, Context> {
	private static final Logger logger = Logger.getLogger(StreamAwareFixedJoinFunction.class);
	
	// Valid during planning, not needed after
	private List<String> ruleVars;
	private List<String> baseVars;
	private Map<String,String> ruleToBaseVarMap;
	private List<AnonimisedRuleVariableHolder> contributors;
	// Valid at all stages
	private List<String> sharedOutVars;
	private Map<String,String> leftVarsToOutVars;
	private Map<String,String> rightVarsToOutVars;
	private BindingsOverflowHandler leftOverflow;
	private BindingsOverflowHandler rightOverflow;
	private String anonimisedDefaultStreamName;
	// Valid at query time, but not needed before
	private FixedHashSteM leftQueue;
	private FixedHashSteM rightQueue;
	private Map<String, List<Context>> outputStreamBuffers;
	
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
		// Construct the ordered list of contibuting atomic VariableHolders
		this.contributors = new ArrayList<AnonimisedRuleVariableHolder>();
		this.contributors.addAll(left.contributors());
		this.contributors.addAll(right.contributors());
		this.contributors = VariableHolderAnonimisationUtils.sortVariableHolders(this.contributors);
		
		// Find the mapping from current rule variables to underlying output variables.
		this.ruleVars = new ArrayList<String>();
		this.baseVars = new ArrayList<String>();
		this.ruleToBaseVarMap = new HashMap<String, String>();
		VariableHolderAnonimisationUtils.extractSaneRuleAndAnonVarsAndMapping(this.contributors,
																			  this.ruleVars,
																			  this.baseVars,
																			  this.ruleToBaseVarMap);
		
		// Construct the anonimised representation of the function.
		StringBuilder anon = new StringBuilder();
		for (AnonimisedRuleVariableHolder atomicVH : VariableHolderAnonimisationUtils.sortVariableHolders(this.contributors)){
			Map<String, String> varmap = new HashMap<String, String>();
			for (String ruleVar : atomicVH.ruleVariables()){
				varmap.put(atomicVH.ruleToBaseVarMap().get(ruleVar), this.ruleToBaseVarMap.get(ruleVar));
			}
			anon.append(atomicVH.anonimised(varmap));
		}
		this.anonimisedDefaultStreamName = anon.toString();
		
		// Find joined underlying output variables from rule variables and their mapping to underlying variables.
		List<String> joinedRuleVars = VariableHolderAnonimisationUtils.extractJoinFields(this.contributors);
		this.sharedOutVars = new ArrayList<String>();
		for (String ruleVar : joinedRuleVars){
			this.sharedOutVars.add(this.ruleToBaseVarMap.get(ruleVar));
		}
		
		// Find the mapping from underlying input variables to underlying output variables. 
		this.leftVarsToOutVars = new HashMap<String, String>();
		this.rightVarsToOutVars = new HashMap<String, String>();
		for (String ruleVar : this.ruleVars){
			String leftInputVar = left.ruleToBaseVarMap().get(ruleVar);
			String rightInputVar = right.ruleToBaseVarMap().get(ruleVar);
			if (leftInputVar != null){
				this.leftVarsToOutVars.put(leftInputVar, this.ruleToBaseVarMap.get(ruleVar));
			}
			if (rightInputVar != null){
				this.rightVarsToOutVars.put(rightInputVar, this.ruleToBaseVarMap.get(ruleVar));
			}
		}
		
		// Set the OverflowHandlers for the left and right queues, as well as explicitly setting the queues to null
		// (queues are created in setup).
		this.leftOverflow = new BindingsOverflowHandler(leftStream, leftwi);
		this.rightOverflow = new BindingsOverflowHandler(rightStream, rightwi);
		this.leftQueue = null;
		this.rightQueue = null;
		
		// Set the output stream buffers to null (created in setup)
		this.outputStreamBuffers = null;
	}
	
	/**
	 * @return
	 * 		The underlying variables received from the left hand source on which this function relies, in specific order.
	 */
	public List<String> leftSharedVars() {
		Map<String, String> outVarsToLeftVars = new HashMap<String, String>();
		for (String leftVar : this.leftVarsToOutVars.keySet()){
			outVarsToLeftVars.put(this.leftVarsToOutVars.get(leftVar), leftVar);
		}
		
		List<String> leftSharedVars = new ArrayList<String>();
		for (String var : this.sharedOutVars){
			leftSharedVars.add(outVarsToLeftVars.get(var));
		}
		return leftSharedVars;
	}
	
	/**
	 * @return
	 * 		The underlying variables received from the left hand source on which this function relies, in specific order.
	 */
	public List<String> rightSharedVars() {
		Map<String, String> outVarsToRightVars = new HashMap<String, String>();
		for (String rightVar : this.rightVarsToOutVars.keySet()){
			outVarsToRightVars.put(this.rightVarsToOutVars.get(rightVar), rightVar);
		}
		
		List<String> rightSharedVars = new ArrayList<String>();
		for (String var : this.sharedOutVars){
			rightSharedVars.add(outVarsToRightVars.get(var));
		}
		return rightSharedVars;
	}
	
	// VARIABLEHOLDER
	
	@Override
	public List<String> variables() {
		if (this.baseVars != null) return this.baseVars;
		
		List<String> baseVars = new ArrayList<String>();
		baseVars.addAll(this.leftVarsToOutVars.values());
		for (String baseVar : this.rightVarsToOutVars.values()){
			if (!baseVars.contains(baseVar)) baseVars.add(baseVar);
		}
		
		return baseVars;
	}
	
	@Override
	public List<String> ruleVariables() {
		return this.ruleVars;
	}

	@Override
	public Map<String, String> ruleToBaseVarMap() {
		return this.ruleToBaseVarMap;
	}
	
	@Override
	public String anonimised() {
		return this.anonimisedDefaultStreamName;
	}
	
	@Override
	public Collection<AnonimisedRuleVariableHolder> contributors() {
		return this.contributors;
	}
	
	@Override
	public boolean mirrorInRule(AnonimisedRuleVariableHolder toMirror) {
		if (toMirror.anonimised() != this.anonimised()) return false;
		
		Map<String,String> mirroredBaseToRuleVarMap = new HashMap<String,String>();
		for (String ruleVar : toMirror.ruleToBaseVarMap().keySet()){
			mirroredBaseToRuleVarMap.put(toMirror.ruleToBaseVarMap().get(ruleVar), ruleVar);
		}
		List<String> newRuleVars = new ArrayList<String>();
		for (String baseVar : this.baseVars){
			String newRuleVar = mirroredBaseToRuleVarMap.get(baseVar);
			if (newRuleVar == null) return false;
			newRuleVars.add(newRuleVar);
		}
		
		this.ruleVars = newRuleVars;
		this.ruleToBaseVarMap = toMirror.ruleToBaseVarMap();
		
		return true;
	}
	
	@Override
	public String anonimised(Map<String, String> varmap) {
		StringBuilder anon = new StringBuilder();
		for (AnonimisedRuleVariableHolder atomicVH : VariableHolderAnonimisationUtils.sortVariableHolders(this.contributors())){
			Map<String, String> subvarmap = new HashMap<String, String>();
			for (String ruleVar : atomicVH.ruleVariables()){
				String aVHBaseVar = atomicVH.ruleToBaseVarMap().get(ruleVar);
				String thisBaseVar = this.ruleToBaseVarMap.get(ruleVar);
				String desiredBaseVar = varmap.get(thisBaseVar);
				subvarmap.put(aVHBaseVar, desiredBaseVar);
			}
			anon.append(atomicVH.anonimised(subvarmap));
		}
		return anon.toString();
	}
	
	// INITIALISABLE
	
	@Override
	public void setup() {
		leftQueue = new FixedHashSteM(this.leftOverflow, this.sharedOutVars, this.leftOverflow.getWindowInformation());
		rightQueue = new FixedHashSteM(this.rightOverflow, this.sharedOutVars, this.rightOverflow.getWindowInformation());
		
		this.outputStreamBuffers = new HashMap<String, List<Context>>();
		for (String name : this.getOutputStreamNames()){
			this.outputStreamBuffers.put(name, new ArrayList<Context>());
		}
	}

	@Override
	public void cleanup() {
		this.ruleVars = null;
		this.baseVars = null;
		this.ruleToBaseVarMap = null;
		this.contributors = null;
		
		this.leftQueue = null;
		this.rightQueue = null;
		this.outputStreamBuffers = null;
	}
	
	// MULTIFUNCTION
	
	@Override
	public List<Context> apply(Context in) {
		this.flushStreamBuffers();
		
		String stream = in.getTyped("stream");
		List<Context> ret = new ArrayList<Context>();
		
		Map<String, Node> typed;
		if(stream.equals(this.leftOverflow.getSource())){
			typed = in.getTyped("bindings");
			logger.debug(String.format("Joining: %s with %s", this, typed));
			
			Map<String, Node> leftbinds = new HashMap<String, Node>();
			for (String var : typed.keySet()){
				leftbinds.put(this.leftVarsToOutVars.get(var), typed.get(var));
			}
			if (leftQueue.build(leftbinds)){
				logger.debug("Joining Left Stream");
				for (Map<String, Node> fullbindings : rightQueue.probe(leftbinds)) {
					logger.debug(String.format("Joined: %s -> %s", typed, fullbindings));
					Context r = new Context();
					r.put("bindings",fullbindings);
					ret.add(r);
				}
			}
		}
		else if(stream.equals(this.rightOverflow.getSource())){
			typed = in.getTyped("bindings");
			logger.debug(String.format("Joining: %s with %s", this, typed));
			
			Map<String, Node> rightbinds = new HashMap<String, Node>();
			for (String var : typed.keySet()){
				rightbinds.put(this.rightVarsToOutVars.get(var), typed.get(var));
			}
			if (rightQueue.build(rightbinds)){
				logger.debug("Joining Right Stream");
				for (Map<String, Node> fullbindings : leftQueue.probe(rightbinds)) {
					logger.debug(String.format("Joined: %s -> %s", typed, fullbindings));
					Context r = new Context();
					r.put("bindings",fullbindings);
					ret.add(r);
				}
			}
		}
		
		this.outputStreamBuffers.get(this.anonimisedDefaultStreamName).addAll(ret);
		
		return ret;
	}
	
	// BUFFEREDOUTPUTSTREAMHOLDER
	
	@Override
	public Set<String> getOutputStreamNames() {
		Set<String> names = new HashSet<String>();
		names.add(this.anonimisedDefaultStreamName);
		names.addAll(this.leftOverflow.getStreamNames());
		names.addAll(this.rightOverflow.getStreamNames());
		return names;
	}
	
	@Override
	public void flushStreamBuffers() {
		for (String name : this.outputStreamBuffers.keySet()){
			this.outputStreamBuffers.get(name).clear();
		}
	}

	@Override
	public List<Context> getStreamBuffer(String streamName) {
		List<Context> buffer = this.outputStreamBuffers.get(streamName);
		if (buffer == null) return new ArrayList<Context>();
		this.outputStreamBuffers.put(streamName, new ArrayList<Context>());
		return buffer;
	}
	
	@Override
	public String toString() {
		Set<String> outVars = new HashSet<String>();
		outVars.addAll(this.leftVarsToOutVars.values());
		outVars.addAll(this.rightVarsToOutVars.values());
		return String.format("JOIN: leftVariables: %s, rightVariables %s, outputVariables: %s, sharedOutputVariables %s",
									this.leftVarsToOutVars.keySet().toString(),
									this.rightVarsToOutVars.keySet().toString(),
									outVars.toString(),
									this.sharedOutVars.toString());
	}
	
	private class BindingsOverflowHandler implements CapacityOverflowHandler<Map<String, Node>>, DurationOverflowHandler<Map<String,Node>> {
		
		private String source;
		private WindowInformation wi;
		
		public BindingsOverflowHandler(String s, WindowInformation wi){
			this.source = s;
			this.wi = wi;
		}
		
		public void updateSource(String s){
			this.source = s;
		}
		
		public String getSource(){
			return this.source;
		}
		
		public WindowInformation getWindowInformation(){
			return this.wi;
		}
		
		private String getCapacityOverflowStreamName(){
			return this.source+"[+"+this.wi.getCapacity()+"]";
		}
		
		private String getDurationOverflowStreamName(){
			return this.source+"[+"+this.wi.getDuration()+this.wi.getGranularity().toString()+"]";
		}
		
		public Set<String> getStreamNames() {
			Set<String> names = new HashSet<String>();
			names.add(this.getCapacityOverflowStreamName());
			names.add(this.getDurationOverflowStreamName());
			return names;
		}
		
		@Override
		public void handleDurationOverflow(Map<String, Node> overflowedBindings) {
			Context overflow = new Context();
			overflow.put("bindings", overflowedBindings);
			StreamAwareFixedJoinFunction.this.outputStreamBuffers.get(this.getDurationOverflowStreamName()).add(overflow);
		}

		@Override
		public void handleCapacityOverflow(Map<String, Node> overflowedBindings) {
			Context overflow = new Context();
			overflow.put("bindings", overflowedBindings);
			StreamAwareFixedJoinFunction.this.outputStreamBuffers.get(this.getCapacityOverflowStreamName()).add(overflow);
		}
		
	}

	
	
}