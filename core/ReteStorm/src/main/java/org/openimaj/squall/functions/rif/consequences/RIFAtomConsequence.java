package org.openimaj.squall.functions.rif.consequences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.rdf.storm.utils.VariableIndependentReteRuleToStringUtils;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IConsequence;
import org.openimaj.squall.compile.data.rif.AbstractRIFFunction;
import org.openimaj.squall.compile.data.rif.BindingsUtils;
import org.openimaj.util.data.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class RIFAtomConsequence extends AbstractRIFFunction implements IConsequence {
	
	private static final Logger logger = Logger.getLogger(RIFAtomConsequence.class);
	private Functor clause;
	private String id;

	/**
	 * @param tp
	 * @param ruleID
	 */
	public RIFAtomConsequence(Functor tp, String ruleID) {
		super();
		Count count = new Count();
		Node[] newArgs = new Node[tp.getArgLength()];
		for (int i = 0; i < tp.getArgs().length; i++){
			newArgs[i] = registerVariable(tp.getArgs()[i], count);
		}
		this.clause = new Functor(tp.getName(), newArgs);
		id = ruleID;
	}
	
	@Override
	public void setSourceVariableHolder(AnonimisedRuleVariableHolder arvh) {
		Map<String, String> ruleToBaseVarMap = this.ruleToBaseVarMap();
		Map<String, String> subRuleToBaseVarMap = arvh.ruleToBaseVarMap();
		Map<String, String> baseToRuleVarMap = new HashMap<String, String>();
		for (String rVar : ruleToBaseVarMap().keySet()){
			baseToRuleVarMap.put(ruleToBaseVarMap.get(rVar), rVar);
		}
		
		Node[] args = new Node[this.clause.getArgLength()];
		for (int i = 0; i < args.length; i++){
			if (this.clause.getArgs()[i].isVariable()){
				args[i] = NodeFactory.createVariable(
								subRuleToBaseVarMap.get(
									baseToRuleVarMap.get(
										this.clause.getArgs()[i].getName()
									)
								)
							);
			} else {
				args[i] = this.clause.getArgs()[i];
			}
		}
		
		this.clause = new Functor(this.clause.getName(), args);
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped("bindings");
		
		List<Functor> ret = new ArrayList<Functor>();
		ret.add(BindingsUtils.instantiate(this.clause,bindings));
		
		List<Context> ctxs = new ArrayList<Context>();
		for (Functor atom : ret) {
			logger.debug(String.format("completed: [%s] -> %s",this.toString(), bindings));
			
			Context out = new Context();
			out.put("atom", atom);
			out.put("rule", this.id);
			ctxs.add(out);			
		}
		return ctxs;
	}

	@Override
	public String identifier() {
		return VariableIndependentReteRuleToStringUtils.clauseEntryToString(clause);
	}
	
	@Override
	public String identifier(Map<String, String> varmap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return String.format("CONSEQUENCE: clause %s",this.clause.toString());
	}
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private RIFAtomConsequence(){}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.clause);
		output.writeString(this.id);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		this.clause = (Functor) kryo.readClassAndObject(input);
		this.id = input.readString();
	}

	@Override
	public boolean isReentrant() {
		return true;
	}

}
