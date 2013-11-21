package org.openimaj.squall.compile.jena;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.OptionalProductionSystems;
import org.openimaj.squall.compile.data.jena.CombinedIVFunction;
import org.openimaj.squall.compile.data.jena.FunctorConsequence;
import org.openimaj.squall.compile.data.jena.FunctorFunction;
import org.openimaj.squall.compile.data.jena.TripleConsequence;
import org.openimaj.squall.compile.data.jena.TripleFilterFunction;
import org.openimaj.squall.data.ISource;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a list of Jena {@link Rule} instances, produce a {@link CompiledProductionSystem}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JenaRuleCompiler implements Compiler<SourceRulePair>{
	
	private final class CombinedContextFunction extends CombinedIVFunction<Context, Context> {
		@Override
		protected List<Context> initial() {
			return new ArrayList<Context>();
		}

		@Override
		protected List<Context> combine(List<Context> out, List<Context> apply) {
			out.addAll(apply);
			return out;
		}
	}

	@Override
	public ContextCPS compile(SourceRulePair sourceRules) {
		List<Rule> rules = sourceRules.secondObject();
		List<ISource<Stream<Context>>> sources = sourceRules.firstObject();
		ContextCPS ret = new ContextCPS();
		ret.setReentrat(false);
		OptionalProductionSystems options = new OptionalProductionSystems();
		ret.addOption(options);
		for (ISource<Stream<Context>> stream : sources) {
			ret.addSource(stream);
		}
		for (Rule rule : rules) {
			if (rule.isAxiom()) {
				// how are we dealing with axioms?
			}
			else
			{
				ContextCPS ruleret = new ContextCPS();
				ruleret.setReentrat(true);
				options.add(ruleret);

				// Extract all the parts of the body
				for (int i = 0; i < rule.bodyLength(); i++) {
					ClauseEntry clause = rule.getBodyElement(i);
					if (clause instanceof TriplePattern) {
						ruleret.addJoinComponent(new TripleFilterFunction((TriplePattern)clause));
					} 
					else if (clause instanceof Functor){
						ruleret.addPredicate(new FunctorFunction(rule,(Functor) clause));
					}
				}
				
				
				CombinedIVFunction<Context, Context> comb = new CombinedContextFunction();
				// Extract all the head parts
				for (int i = 0; i < rule.headLength(); i++) {
					ClauseEntry clause = rule.getHeadElement(i);
					if (clause instanceof TriplePattern) {
						comb.addFunction(new TripleConsequence(rule, (TriplePattern)clause));
					} 
					else if (clause instanceof Functor){
						comb.addFunction(new FunctorConsequence(rule, (Functor)clause));
					}	
				}
				ruleret.addConsequence(comb);
			}
		}
		return ret;
	}

}
