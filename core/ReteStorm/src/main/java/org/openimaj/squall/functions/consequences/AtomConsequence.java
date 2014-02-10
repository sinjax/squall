package org.openimaj.squall.functions.consequences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.squall.compile.data.BindingsUtils;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

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
public class AtomConsequence extends BaseConsequenceFunction {
	
	private static final Logger logger = Logger.getLogger(AtomConsequence.class);
	
	/**
	 * @param clause
	 * @param rID
	 * @return
	 */
	public static RuleWrappedRIFAtomConsequence ruleWrapped(Functor clause, String rID){
		return new RuleWrappedRIFAtomConsequence(clause, rID);
	}
	
	private Functor clause;

	/**
	 * @param tp
	 * @param ruleID
	 */
	public AtomConsequence(Functor tp, String ruleID) {
		super(ruleID);
		this.clause = tp;
	}
	
	@Override
	public AtomConsequence clone() throws CloneNotSupportedException {
		AtomConsequence ac = (AtomConsequence) super.clone();
		ac.clause = new Functor(ac.clause.getName(), ac.clause.getArgs().clone());
		return ac;
	}
	
	protected Functor getClause(){
		return this.clause;
	}
	
	@Override
	public void mapVarNames(Map<String, String> varMap) {
		Node[] args = this.clause.getArgs();
		for (int i = 0; i < args.length; i++){
			if (args[i].isVariable()){
				args[i] = NodeFactory.createVariable(varMap.get(args[i].getName()));
			}
		}
		
		this.clause = new Functor(this.clause.getName(), args);
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		
		List<Functor> ret = new ArrayList<Functor>();
		ret.add(BindingsUtils.instantiate(this.clause,bindings));
		
		List<Context> ctxs = new ArrayList<Context>();
		for (Functor atom : ret) {
			logger.debug(String.format("completed: [%s] -> %s",this.toString(), bindings));
			
			Context out = new Context();
			out.put(ContextKey.ATOM_KEY.toString(), atom);
			out.put("rule", super.getRuleID());
			ctxs.add(out);			
		}
		return ctxs;
	}
	
	@SuppressWarnings("unused") // required for deserialisation by reflection
	private AtomConsequence(){
		super("No Rule Name");
	}

	@Override
	public void write(Kryo kryo, Output output) {
		super.write(kryo, output);
		kryo.writeClassAndObject(output, this.clause);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		super.read(kryo, input);
		this.clause = (Functor) kryo.readClassAndObject(input);
	}

	@Override
	public boolean isReentrant() {
		return true;
	}

	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static class RuleWrappedRIFAtomConsequence extends RuleWrappedConsequenceFunction<AtomConsequence> {
		
		protected RuleWrappedRIFAtomConsequence(Functor clause, String rID){
			super(new AtomConsARVH(clause, rID));
			this.wrap(new AtomConsequence(((AtomConsARVH) super.getVariableHolder()).clause, rID));
		}
		
		/**
		 * @author davidlmonks
		 *
		 */
		public static class AtomConsARVH extends ConsequenceARVH {

			private final Functor clause;
			
			protected AtomConsARVH(Functor clause, String rID) {
				super(rID);
				this.clause = clause;
			}
			
			@Override
			public String identifier(Map<String, String> varmap) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String identifier() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String toString() {
				return String.format("CONSEQUENCE: clause %s",this.clause.toString());
			}
			
		}
		
	}
	
}
