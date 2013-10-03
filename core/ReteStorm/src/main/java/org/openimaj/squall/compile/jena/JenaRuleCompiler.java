package org.openimaj.squall.compile.jena;

import java.util.List;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.TripleTripleCPS;
import org.openimaj.squall.compile.data.FunctorFunction;
import org.openimaj.squall.compile.data.TripleConsequence;
import org.openimaj.squall.compile.data.TripleFilterFunction;

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
public class JenaRuleCompiler implements Compiler<Triple,Triple,List<Rule>>{
	
	@Override
	public TripleTripleCPS compile(List<Rule> rules) {
		TripleTripleCPS ret = new TripleTripleCPS();
		for (Rule rule : rules) {
			if (rule.isAxiom()) {
				// how are we dealing with axioms?
			}
			else
			{
				TripleTripleCPS ruleret = new TripleTripleCPS();
				ret.addSystem(ruleret);

				// Extract all the parts of the body
				for (int i = 0; i < rule.bodyLength(); i++) {
					ClauseEntry clause = rule.getBodyElement(i);
					if (clause instanceof TriplePattern) {
						ruleret.addFilter(new TripleFilterFunction((TriplePattern)clause));
					} 
					else if (clause instanceof Functor){
						ruleret.addPredicate(new FunctorFunction((Functor) clause));
					}
				}
				
				// Extract all the head parts
				for (int i = 0; i < rule.headLength(); i++) {
					ClauseEntry clause = rule.getHeadElement(i);
					ruleret.addConsequence(new TripleConsequence((TriplePattern)clause));
				}
			}
		}
		return ret;
	}

}
