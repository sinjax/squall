package org.openimaj.squall.orchestrate.greedy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.OverflowHandler.CapacityOverflowHandler;
import org.openimaj.rdf.storm.utils.OverflowHandler.DurationOverflowHandler;
import org.openimaj.squall.orchestrate.NamedNode;
import org.openimaj.squall.orchestrate.WindowInformation;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IFunction;
import org.openimaj.squall.compile.data.RuleWrappedFunction;
import org.openimaj.squall.compile.data.SIFunction;
import org.openimaj.squall.compile.data.VariableHolderAnonimisationUtils;
import org.openimaj.squall.data.RuleWrapped;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
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
public class StreamAwareFixedJoinFunction implements SIFunction<Context, Context> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7946505511340986483L;

	private static final Logger logger = Logger.getLogger(StreamAwareFixedJoinFunction.class);
	
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
	private Collection<Context> outputBuffer;
	
	/**
	 * @param left
	 * @param right
	 * @return
	 */
	public static RuleWrappedStreamAwareFixedJoinFunction ruleWrapped(
						RuleWrapped<?> left,
						RuleWrapped<?> right
	){
		return new RuleWrappedStreamAwareFixedJoinFunction(left, right);
	}
	
	/**
	 * @param sharedOutVars 
	 * @param leftVarsToOutVars 
	 * @param rightVarsToOutVars 
	 * @param anonimisedDefaultStreamName 
	 */
	public StreamAwareFixedJoinFunction(
			List<String> sharedOutVars,
			Map<String, String> leftVarsToOutVars,
			Map<String, String> rightVarsToOutVars,
			String anonimisedDefaultStreamName
	) {
		this.sharedOutVars = sharedOutVars;
		this.leftVarsToOutVars = leftVarsToOutVars;
		this.rightVarsToOutVars = rightVarsToOutVars;
		this.anonimisedDefaultStreamName = anonimisedDefaultStreamName;
		
		// set all other variables explicitly to null
		this.leftOverflow = null;
		this.rightOverflow = null;
		this.leftQueue = null;
		this.rightQueue = null;
		
		// Set the output stream buffers to null (created in setup)
		this.outputBuffer = null;
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
	
	/**
	 * updates the name of the source stream feeding the left side of the join.
	 * @param lsn - new stream name.
	 * @param wi 
	 */
	public void setLeftStream(String lsn, WindowInformation wi){
		if (this.leftOverflow == null){
			this.leftOverflow = new BindingsOverflowHandler(lsn, wi);
		} else {
			throw new RuntimeException("Cannot have more than one stream feeding the lefthand side of a Join.");
		}
	}
	
	/**
	 * updates the name of the source stream feeding the right side of the join.
	 * @param rsn - new stream name
	 * @param wi 
	 */
	public void setRightStream(String rsn, WindowInformation wi){
		if (this.rightOverflow == null){
			this.rightOverflow = new BindingsOverflowHandler(rsn, wi);
		} else {
			throw new RuntimeException("Cannot have more than one stream feeding the righthand side of a Join.");
		}
	}
	
	// INITIALISABLE
	
	@Override
	public void setup() {
		leftQueue = new FixedHashSteM(this.leftOverflow, this.sharedOutVars, this.leftOverflow.getWindowInformation());
		rightQueue = new FixedHashSteM(this.rightOverflow, this.sharedOutVars, this.rightOverflow.getWindowInformation());
		
		this.outputBuffer = new ArrayList<Context>();
	}

	@Override
	public void cleanup() {
		this.leftQueue = null;
		this.rightQueue = null;
		this.outputBuffer = null;
	}
	
	private StreamAwareFixedJoinFunction(){
		this.leftQueue = null;
		this.rightQueue = null;
		this.outputBuffer = null;
	}
	
	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(this.sharedOutVars.size());
		for (int i = 0; i < this.sharedOutVars.size(); i++){
			output.writeString(this.sharedOutVars.get(i));
		}
		
		output.writeInt(this.leftVarsToOutVars.size());
		for (String key : this.leftVarsToOutVars.keySet()){
			output.writeString(key);
			output.writeString(this.leftVarsToOutVars.get(key));
		}
		
		output.writeInt(this.rightVarsToOutVars.size());
		for (String key : this.rightVarsToOutVars.keySet()){
			output.writeString(key);
			output.writeString(this.rightVarsToOutVars.get(key));
		}
		
		output.writeString(this.leftOverflow.getSource());
		output.writeInt(this.leftOverflow.getWindowInformation().getCapacity());
		output.writeLong(this.leftOverflow.getWindowInformation().getDuration());
		kryo.writeClassAndObject(output, this.leftOverflow.getWindowInformation().getGranularity());
		output.writeBoolean(this.leftOverflow.getWindowInformation().isOverriding());
		
		output.writeString(this.rightOverflow.getSource());
		output.writeInt(this.rightOverflow.getWindowInformation().getCapacity());
		output.writeLong(this.rightOverflow.getWindowInformation().getDuration());
		kryo.writeClassAndObject(output, this.rightOverflow.getWindowInformation().getGranularity());
		output.writeBoolean(this.rightOverflow.getWindowInformation().isOverriding());
		
		output.writeString(this.anonimisedDefaultStreamName);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		int size = input.readInt();
		this.sharedOutVars = new ArrayList<>(size);
		for (int i = 0; i < size; i++){
			this.sharedOutVars.add(input.readString());
		}
		
		size = input.readInt();
		this.leftVarsToOutVars = new HashMap<String,String>(size);
		for (int i = 0; i < size; i++){
			this.leftVarsToOutVars.put(
					input.readString(),
					input.readString()
			);
		}
		
		size = input.readInt();
		this.rightVarsToOutVars = new HashMap<String,String>(size);
		for (int i = 0; i < size; i++){
			this.rightVarsToOutVars.put(
					input.readString(),
					input.readString()
			);
		}
		
		String leftSource = input.readString();
		int leftCap = input.readInt();
		long leftDur = input.readLong();
		TimeUnit leftUnit = (TimeUnit) kryo.readClassAndObject(input);
		boolean leftOverride = input.readBoolean();
		this.leftOverflow = new BindingsOverflowHandler(leftSource, new WindowInformation(leftOverride,leftCap,leftDur,leftUnit));
		
		String rightSource = input.readString();
		int rightCap = input.readInt();
		long rightDur = input.readLong();
		TimeUnit rightUnit = (TimeUnit) kryo.readClassAndObject(input);
		boolean rightOverride = input.readBoolean();
		this.rightOverflow = new BindingsOverflowHandler(rightSource, new WindowInformation(rightOverride,rightCap,rightDur,rightUnit));
		
		this.anonimisedDefaultStreamName = input.readString();
	}
	
	@Override
	public boolean isStateless() {
		return false;
	}

	@Override
	public boolean forcedUnique() {
		return false;
	}
	
	// MULTIFUNCTION
	
	private Map<String, Node> getOutVarsMapping(Map<String,String> inToOutMapping, Map<String, Node> inBinds){
		Map<String, Node> leftbinds = new HashMap<String, Node>();
		for (String var : inBinds.keySet()){
			leftbinds.put(inToOutMapping.get(var), inBinds.get(var));
		}
		return leftbinds;
	}
	
	private List<Context> buildAndProbe(Map<String, Node> inBinds, FixedHashSteM buildSteM, FixedHashSteM probeSteM){
		List<Context> ret = new ArrayList<Context>();
		if (buildSteM.build(inBinds)){
			logger.debug("Joining Left Stream");
			for (Map<String, Node> fullbindings : probeSteM.probe(inBinds)) {
				logger.debug(String.format("Joined: %s -> %s", inBinds, fullbindings));
				Context r = new Context();
				r.put(ContextKey.BINDINGS_KEY.toString(),fullbindings);
				ret.add(r);
			}
		}
		return ret;
	}
	
	@Override
	public List<Context> apply(Context in) {
		String stream = in.getTyped(ContextKey.STREAM_KEY.toString());
		logger.debug(String.format("JOIN: Received input from %s, checking against %s and %s",
										stream,
										this.leftOverflow.getSource(),
										this.rightOverflow.getSource()
								)
					);
		List<Context> ret = new ArrayList<Context>();
		
		Map<String, Node> typed = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		logger.debug(String.format("Joining: %s with %s", this, typed));
		if(stream.equals(this.leftOverflow.getSource())){
			Map<String, Node> leftbinds = this.getOutVarsMapping(this.leftVarsToOutVars, typed);
			ret = this.buildAndProbe(leftbinds, this.leftQueue, this.rightQueue);
		}
		else if(stream.equals(this.rightOverflow.getSource())){
			Map<String, Node> rightbinds = this.getOutVarsMapping(this.rightVarsToOutVars, typed);
			ret = this.buildAndProbe(rightbinds, this.rightQueue, this.leftQueue);
		}
		
		ret.addAll(this.outputBuffer);
		this.outputBuffer.clear();
		
		return ret;
	}
	
	// STREAMS
	
	public Set<String> getOutputStreamNames(){
		Set<String> names = this.leftOverflow.getStreamNames();
		names.addAll(this.rightOverflow.getStreamNames());
		names.add(anonimisedDefaultStreamName);
		return names;
	}
	
	// OBJECT
	
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
	
	// private nested class
	
	private class BindingsOverflowHandler implements CapacityOverflowHandler<Map<String, Node>>, DurationOverflowHandler<Map<String,Node>> {
		
		private String source;
		private WindowInformation wi;
		
		public BindingsOverflowHandler(String s, WindowInformation wi){
			this.source = s;
			this.wi = wi;
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
			overflow.put(ContextKey.BINDINGS_KEY.toString(), overflowedBindings);
			overflow.put(ContextKey.STREAM_KEY.toString(), this.getDurationOverflowStreamName());
			StreamAwareFixedJoinFunction.this.outputBuffer.add(overflow);
		}

		@Override
		public void handleCapacityOverflow(Map<String, Node> overflowedBindings) {
			Context overflow = new Context();
			overflow.put(ContextKey.BINDINGS_KEY.toString(), overflowedBindings);
			overflow.put(ContextKey.STREAM_KEY.toString(), this.getCapacityOverflowStreamName());
			StreamAwareFixedJoinFunction.this.outputBuffer.add(overflow);
		}
		
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 * 
	 * Wrapper that holds rule information
	 *
	 */
	public static class RuleWrappedStreamAwareFixedJoinFunction extends RuleWrappedFunction<StreamAwareFixedJoinFunction> {
		
		private RuleWrappedStreamAwareFixedJoinFunction(
						RuleWrapped<?> left,
						RuleWrapped<?> right
					){
			super(new JoinFuncARVH(left, right));
			JoinFuncARVH jfarvh = (JoinFuncARVH) super.getVariableHolder();
			this.wrap(new StreamAwareFixedJoinFunction(
							jfarvh.sharedOutVars,
							jfarvh.leftVarsToOutVars,
							jfarvh.rightVarsToOutVars,
							jfarvh.anonimisedDefaultStreamName
					));
		}
		
		protected static class JoinFuncARVH extends ARVHComponent {
			
			private final List<AnonimisedRuleVariableHolder> contributors;
			private final List<String> sharedOutVars;
			private final Map<String, String> leftVarsToOutVars;
			private final Map<String, String> rightVarsToOutVars;
			private final String anonimisedDefaultStreamName;
			
			protected JoinFuncARVH(
							RuleWrapped<?> left,
							RuleWrapped<?> right
						){
				// Construct the ordered list of contibuting atomic VariableHolders
				List<AnonimisedRuleVariableHolder> prelimContributors = new ArrayList<AnonimisedRuleVariableHolder>();
				prelimContributors.addAll(left.contributors());
				prelimContributors.addAll(right.contributors());
				this.contributors = VariableHolderAnonimisationUtils.sortVariableHolders(prelimContributors);
				
				// Find the mapping from current rule variables to underlying output variables.
				VariableHolderAnonimisationUtils.extractSaneRuleAndAnonVarsAndMapping(this);
				
				// Construct the anonimised representation of the function.
				StringBuilder anon = new StringBuilder();
				for (AnonimisedRuleVariableHolder atomicVH : this.contributors){
					Map<String, String> varmap = new HashMap<String, String>();
					for (String ruleVar : atomicVH.ruleVariables()){
						varmap.put(atomicVH.ruleToBaseVarMap().get(ruleVar), this.ruleToBaseVarMap().get(ruleVar));
					}
					anon.append(atomicVH.identifier(varmap));
				}
				this.anonimisedDefaultStreamName = anon.toString();
				
				// Find joined underlying output variables from rule variables and their mapping to underlying variables.
				List<String> joinedRuleVars = VariableHolderAnonimisationUtils.extractJoinFields(this.contributors);
				this.sharedOutVars = new ArrayList<String>();
				for (String ruleVar : joinedRuleVars){
					if (left.ruleToBaseVarMap().containsKey(ruleVar) && right.ruleToBaseVarMap().containsKey(ruleVar)){
						this.sharedOutVars.add(this.ruleToBaseVarMap().get(ruleVar));
					}
				}
				
				// Find the mapping from underlying input variables to underlying output variables. 
				this.leftVarsToOutVars = new HashMap<String, String>();
				this.rightVarsToOutVars = new HashMap<String, String>();
				for (String ruleVar : this.ruleVariables()){
					String leftInputVar = left.ruleToBaseVarMap().get(ruleVar);
					String rightInputVar = right.ruleToBaseVarMap().get(ruleVar);
					if (leftInputVar != null){
						this.leftVarsToOutVars.put(leftInputVar, this.ruleToBaseVarMap().get(ruleVar));
					}
					if (rightInputVar != null){
						this.rightVarsToOutVars.put(rightInputVar, this.ruleToBaseVarMap().get(ruleVar));
					}
				}
			}
			
			@Override
			public Collection<AnonimisedRuleVariableHolder> contributors() {
				return this.contributors;
			}
			
			@Override
			public String identifier() {
				return this.anonimisedDefaultStreamName;
			}
			
			@Override
			public String identifier(Map<String, String> varmap) {
				StringBuilder anon = new StringBuilder();
				for (AnonimisedRuleVariableHolder atomicVH : VariableHolderAnonimisationUtils.sortVariableHolders(this.contributors())){
					Map<String, String> subvarmap = new HashMap<String, String>();
					for (String ruleVar : atomicVH.ruleVariables()){
						String aVHBaseVar = atomicVH.ruleToBaseVarMap().get(ruleVar);
						String thisBaseVar = this.ruleToBaseVarMap().get(ruleVar);
						String desiredBaseVar = varmap.get(thisBaseVar);
						subvarmap.put(aVHBaseVar, desiredBaseVar);
					}
					anon.append(atomicVH.identifier(subvarmap));
				}
				return anon.toString();
			}
			
		}
		
	}
	
}