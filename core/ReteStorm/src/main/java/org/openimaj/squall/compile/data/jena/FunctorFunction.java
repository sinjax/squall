package org.openimaj.squall.compile.data.jena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.topology.rules.ReteTopologyRuleContext;
import org.openimaj.squall.compile.data.AnonimisedRuleVariableHolder;
import org.openimaj.squall.compile.data.IPredicate;
import org.openimaj.util.data.Context;
import org.openimaj.util.data.ContextKey;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.reasoner.rulesys.impl.BindingVector;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * Given a binding environment, output a binding environment
 *
 */
public class FunctorFunction extends AbstractFunctorFunction<Context,Context> implements IPredicate {
	
	private Rule rule;
	
	/**
	 * @param r
	 * @param clause
	 */
	public FunctorFunction(Rule r, Functor clause) {
		super(r, clause);
		this.rule = r;
	}

	@Override
	public List<Context> apply(Context in) {
		List<Context> ret = new ArrayList<Context>();
		Map<String,Node> object = in.getTyped(ContextKey.BINDINGS_KEY.toString());
		BindingVector be = mapToB(object);
		RuleContext context = new ReteTopologyRuleContext.IgnoreAdd(this.rule, be);
		if(!clause.evalAsBodyClause(context)) return ret;
		
		ret.add(new Context(ContextKey.BINDINGS_KEY.toString(), bToMap(be)));
		return ret;
	}

	// required for deserialisation by reflection
	private FunctorFunction(){
		super(null, null);
	}

	@Override
	public void write(Kryo kryo, Output output) {
		super.write(kryo, output);
		kryo.writeClassAndObject(output, this.rule);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		super.read(kryo, input);
		this.rule = (Rule) kryo.readClassAndObject(input);
	}

	@Override
	public void setSourceVariableHolder(AnonimisedRuleVariableHolder arvh) {}
	
}