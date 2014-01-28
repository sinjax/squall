package org.openimaj.squall.compile.data.jena;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.util.data.Context;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class AbstractTripleFunction extends IVFunction<Context, Context> {
	private final static Logger logger = Logger.getLogger(AbstractTripleFunction.class);

	protected TriplePattern clause;
	protected Node_RuleVariable[] ruleVariables;
	protected Triple extended;
	
	/**
	 * @param r 
	 * @param clause construct using a {@link TriplePattern}
	 */
	public AbstractTripleFunction(Rule r, TriplePattern clause) {
		super();
		if (clause != null){
			this.clause = clause;
			this.extended = asExtendedTripleMatch(clause).asTriple();
		}
		if (r != null){
			this.ruleVariables = BindingsUtils.extractRuleVariables(r);
		}
	}
	
	private TripleMatch asExtendedTripleMatch(TriplePattern tp){
		Triple created = new Triple(
			registerVariable(tp.getSubject()),
			registerVariable(tp.getPredicate()),
			registerVariable(tp.getObject())
		);
		return created;
	}
	
	private Node registerVariable(Node n) {
		if(n.isVariable()){
			this.addVariable(n.getName());
			this.putRuleToBaseVarMapEntry(n.getName(), n.getName());
			return Node.ANY ;
		}
		else if(Functor.isFunctor(n)){
			Functor f = (Functor)n.getLiteralValue();
			Node[] newArgs = new Node[f.getArgLength()];
			for (int i = 0; i < f.getArgs().length; i++){
				newArgs[i] = registerVariable(f.getArgs()[i]);
			}
			return Functor.makeFunctorNode(f.getName(), newArgs);
		}
		return n;
	}

	protected String stringifyNode(Node node){
		return node.isVariable() ? "?"+this.indexOfVar(node.getName()) : node.toString();
	}
	
	protected String mapNode(Map<String,String> varmap, Node node){
		String nodeString = this.stringifyNode(node);
		String mappedString;
		return node.isVariable()
				? (mappedString = varmap.get(nodeString)) == null
					? "VAR"
					: mappedString
				: nodeString;
	}
	
	@Override
	public String identifier() {
		String subject = this.stringifyNode(this.clause.getSubject()),
			   predicate = this.stringifyNode(this.clause.getPredicate()),
			   object;
		if (Functor.isFunctor(this.clause.getObject())){
			StringBuilder obj = new StringBuilder();
			Functor f = (Functor) this.clause.getObject().getLiteralValue();
			obj.append(f.getName()).append("(")
			   .append(this.stringifyNode(f.getArgs()[0]));
			for (int i = 1; i < f.getArgLength(); i++){
				obj.append(",").append(this.stringifyNode(f.getArgs()[i]));
			}
			obj.append(")");
			object = obj.toString();
		}else{
			object = this.stringifyNode(this.clause.getObject());
		}
		
		StringBuilder name = new StringBuilder("(");
		name.append(subject).append(" ")
			.append(predicate).append(" ")
			.append(object).append(")");
		return name.toString();
	}
	
	@Override
	public String identifier(Map<String, String> varmap) {
		String subject = this.mapNode(varmap, this.clause.getSubject()),
			   predicate = this.mapNode(varmap, this.clause.getPredicate()),
			   object;
		if (Functor.isFunctor(this.clause.getObject())){
			StringBuilder obj = new StringBuilder();
			Functor f = (Functor) this.clause.getObject().getLiteralValue();
			obj.append(f.getName()).append("(")
			   .append(this.mapNode(varmap, f.getArgs()[0]));
			for (int i = 1; i < f.getArgLength(); i++){
				obj.append(",").append(this.mapNode(varmap, f.getArgs()[i]));
			}
			obj.append(")");
			object = obj.toString();
		}else{
			object = this.mapNode(varmap, this.clause.getObject());
		}
		
		StringBuilder name = new StringBuilder("(");
		name.append(subject).append(" ")
			.append(predicate).append(" ")
			.append(object).append(")");
		return name.toString();
	}
	
	@Override
	public void setup() {}
	@Override
	public void cleanup() {}
	
	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeClassAndObject(output, this.clause);
		output.writeInt(this.ruleVariables.length);
		for (int i = 0; i < this.ruleVariables.length; i++){
			kryo.writeClassAndObject(output, this.ruleVariables[i]);
		}
	}

	@Override
	public void read(Kryo kryo, Input input) {
		this.clause = (TriplePattern) kryo.readClassAndObject(input);
		
		Node object = this.clause.getObject();
		if(Functor.isFunctor(object)){
			Functor f = (Functor)object.getLiteralValue();
			Node[] newArgs = new Node[f.getArgLength()];
			for (int i = 0; i < f.getArgs().length; i++){
				newArgs[i] = registerVariable(f.getArgs()[i]);
			}
			object = Functor.makeFunctorNode(f.getName(), newArgs);
		}
		this.extended = new Triple(
				this.clause.getSubject().isVariable() ? Node.ANY : this.clause.getSubject(),
				this.clause.getPredicate().isVariable() ? Node.ANY : this.clause.getPredicate(),
				this.clause.getObject().isVariable() ? Node.ANY : this.clause.getObject()
			);
		
		this.ruleVariables = new Node_RuleVariable[input.readInt()];
		for (int i = 0; i < this.ruleVariables.length; i++){
			this.ruleVariables[i] = (Node_RuleVariable) kryo.readClassAndObject(input);
		}
	}
	
	@Override
	public boolean isStateless() {
		return true;
	}
	
	@Override
	public boolean forcedUnique() {
		return false;
	}

}
