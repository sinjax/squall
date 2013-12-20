package org.openimaj.squall.orchestrate.revised;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openimaj.squall.compile.data.revised.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.revised.IVFunction;
import org.openimaj.squall.compile.data.revised.VariableHolder;
import org.openimaj.util.data.Context;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *@author David Monks <dm11g08@ecs.soton.ac.uk>
 */
public class RevisedWrappedIVFunction implements IVFunction<Context,Context>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1533515752338530090L;
	private RevisedNamedNode<IVFunction<Context, Context>> nn;
	private IVFunction<Context,Context> func;

	/**
	 * @param func
	 * @param nn
	 */
	public RevisedWrappedIVFunction(IVFunction<Context,Context> func, RevisedNamedNode<IVFunction<Context,Context>> nn) {
		this.nn = nn;
		this.func = func;
	}
	
	@Override
	public List<Context> apply(Context in) {
		List<Context> ret = this.func.apply(in);
		if(ret == null) return null;
		for (Context ctx : ret) {
			nn.addName(ctx);
		}
		return ret;
	}

	@Override
	public List<String> variables() {
		return func.variables();
	}

	@Override
	public List<String> ruleVariables() {
		return func.ruleVariables();
	}

	@Override
	public Map<String, String> ruleToBaseVarMap() {
		return func.ruleToBaseVarMap();
	}

	@Override
	public boolean mirrorInRule(AnonimisedRuleVariableHolder toMirror) {
		return func.mirrorInRule(toMirror);
	}

	@Override
	public String anonimised(Map<String, String> varmap) {
		return func.anonimised(varmap);
	}

	@Override
	public Collection<AnonimisedRuleVariableHolder> contributors() {
		return func.contributors();
	}

	@Override
	public String anonimised() {
		return func.anonimised();
	}

	@Override
	public void setup() {
		func.setup();
	}

	@Override
	public void cleanup() {
		func.cleanup();
	}
	
	@Override
	public String toString() {
		return func.toString();
	}

	
}