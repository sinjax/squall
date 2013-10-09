package org.openimaj.squall.compile.jena;

import java.util.List;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.TripleTripleListCPS;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.jena.FunctorConsequence;
import org.openimaj.squall.compile.data.jena.FunctorFunction;
import org.openimaj.squall.compile.data.jena.TripleConsequence;
import org.openimaj.squall.compile.data.jena.TripleFilterFunction;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a list of Jena {@link Rule} instances, produce a {@link CompiledProductionSystem}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JenaRuleCompiler implements Compiler<Triple,List<Triple>,SourceRulePair>{
	
	@Override
	public TripleTripleListCPS compile(SourceRulePair sourceRules) {
		List<Rule> rules = sourceRules.secondObject();
		List<IStream<Context>> sources = sourceRules.firstObject();
		TripleTripleListCPS ret = new TripleTripleListCPS();
		for (IStream<Context> stream : sources) {
			ret.addSource(stream);
		}
		for (Rule rule : rules) {
			if (rule.isAxiom()) {
				// how are we dealing with axioms?
			}
			else
			{
				ContextCPS ruleret = new ContextCPS();
				ret.addSystem(ruleret);

				// Extract all the parts of the body
				for (int i = 0; i < rule.bodyLength(); i++) {
					ClauseEntry clause = rule.getBodyElement(i);
					if (clause instanceof TriplePattern) {
						ruleret.addFilter(new TripleFilterFunction((TriplePattern)clause));
					} 
					else if (clause instanceof Functor){
						ruleret.addPredicate(new FunctorFunction(rule,(Functor) clause));
					}
				}
				
				// Extract all the head parts
				for (int i = 0; i < rule.headLength(); i++) {
					ClauseEntry clause = rule.getHeadElement(i);
					if (clause instanceof TriplePattern) {
						ruleret.addConsequence(new TripleConsequence(rule, (TriplePattern)clause));
					} 
					else if (clause instanceof Functor){
						ruleret.addConsequence(new FunctorConsequence(rule, (Functor)clause));
					}
					
				}
			}
		}
		return ret;
	}

}
