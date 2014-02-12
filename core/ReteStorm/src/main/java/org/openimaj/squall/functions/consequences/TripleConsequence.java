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
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("serial")
public class TripleConsequence extends BaseConsequenceFunction {

	private static final Logger logger = Logger.getLogger(TripleConsequence.class);
	
	/**
	 * @param clause
	 * @param rID
	 * @return
	 */
	public static RuleWrappedRIFTripleConsequence ruleWrapped(TriplePattern clause, String rID){
		return new RuleWrappedRIFTripleConsequence(new TripleConsARVH(clause, rID));
	}
	
	private TriplePattern clause;

	/**
	 * @param clause 
	 * @param ruleID
	 */
	public TripleConsequence(TriplePattern clause, String ruleID) {
		super(ruleID);
		this.clause = clause;
	}
	
	@Override
	public TripleConsequence clone() throws CloneNotSupportedException {
		TripleConsequence clone = (TripleConsequence) super.clone();
		clone.clause = new TriplePattern(
							clone.clause.getSubject(),
							clone.clause.getPredicate(),
							clone.clause.getObject()
						);
		return clone;
	}
	
	private Node getMappedNode(Node node, Map<String, String> varmap){
		if (node.isVariable()){
			return NodeFactory.createVariable(
						varmap.get(
							node.getName()
						)
					);
		} else {
			return node;
		}
	}
	
	@Override
	public void mapVarNames(Map<String, String> varMap) {
		this.clause = new TriplePattern(
				getMappedNode(this.clause.getSubject(), varMap),
				getMappedNode(this.clause.getPredicate(), varMap),
				getMappedNode(this.clause.getObject(), varMap)
			);
	}

	@Override
	public List<Context> apply(Context in) {
		Map<String,Node> bindings = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		
		List<Triple> ret = new ArrayList<Triple>();
		ret.add(BindingsUtils.instantiate(this.clause,bindings));
		
		List<Context> ctxs = new ArrayList<Context>();
		for (Triple triple : ret) {
			logger.debug(String.format("completed: [%s] -> %s",this.toString(), bindings));
			
			Context out = new Context();
			out.put(ContextKey.TRIPLE_KEY.toString(), triple);
			out.put("rule", super.getRuleID());
			ctxs.add(out);			
		}
		return ctxs;
	}

	private TripleConsequence(){ // required for deserialisation by reflection
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
		this.clause = (TriplePattern) kryo.readClassAndObject(input);
	}

	@Override
	public boolean isReentrant() {
		return true;
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static class RuleWrappedRIFTripleConsequence extends RuleWrappedConsequenceFunction<TripleConsequence> {
		
		protected RuleWrappedRIFTripleConsequence(TripleConsARVH arvh) {
			super(arvh);
			super.wrap(new TripleConsequence(arvh.clause, arvh.getRuleID()));
		}
		
	}
	
	protected static class TripleConsARVH extends ConsequenceARVH {

		private TriplePattern clause;
		
		public TripleConsARVH(TriplePattern clause, String rID) {
			super(rID);
			Count count = new Count();
			this.clause = new TriplePattern(
				registerVariable(clause.getSubject(), count),
				registerVariable(clause.getPredicate(), count),
				registerVariable(clause.getObject(), count)
			);
		}
		
		@Override
		public TripleConsARVH clone() throws CloneNotSupportedException {
			TripleConsARVH clone = (TripleConsARVH) super.clone();
			clone.clause = new TriplePattern(
								clone.clause.getSubject(),
								clone.clause.getPredicate(),
								clone.clause.getObject()
							);
			return clone;
		}
		
		@Override
		protected Node registerVariable(Node n, Count count) {
			n = super.registerVariable(n, count);
			if(Functor.isFunctor(n)){
				Functor f = (Functor)n.getLiteralValue();
				Node[] newArgs = new Node[f.getArgLength()];
				for (int i = 0; i < f.getArgs().length; i++){
					newArgs[i] = super.registerVariable(f.getArgs()[i], count);
				}
				return Functor.makeFunctorNode(f.getName(), newArgs);
			}
			return n;
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