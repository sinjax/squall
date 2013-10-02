package org.openimaj.squall.compile.jena;

import java.util.List;

import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.data.FunctorPredicate;
import org.openimaj.squall.compile.data.TripleConsequence;
import org.openimaj.squall.compile.data.TripleFilterFunction;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Given a list of Jena {@link Rule} instances, produce a {@link CompiledProductionSystem}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class JenaRuleCompiler implements Compiler<List<Rule>>{
	
	@Override
	public CompiledProductionSystem compile(List<Rule> rules) {
		CompiledProductionSystem ret = new CompiledProductionSystem();
		for (Rule rule : rules) {
			if (rule.isAxiom()) {
				// how are we dealing with axioms?
			}
			else
			{
				CompiledProductionSystem ruleret = new CompiledProductionSystem();
				ret.addSystem(ruleret);

				// Extract all the parts of the body
				for (int i = 0; i < rule.bodyLength(); i++) {
					ClauseEntry clause = rule.getBodyElement(i);
					if (clause instanceof TriplePattern) {
						ruleret.addFilter(new TripleFilterFunction((TriplePattern)clause));
					} 
					else if (clause instanceof Functor){
						ruleret.addPredicate(new FunctorPredicate((Functor) clause));
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
