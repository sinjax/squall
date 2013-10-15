package org.openimaj.squall.compile.rif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rdf.storm.utils.Count;
import org.openimaj.rif.*;
import org.openimaj.rif.conditions.*;
import org.openimaj.rif.conditions.atomic.*;
import org.openimaj.rif.conditions.data.datum.RIFConst;
import org.openimaj.rif.conditions.data.datum.RIFDatum;
import org.openimaj.rif.conditions.data.datum.RIFExternal;
import org.openimaj.rif.conditions.data.datum.RIFIRIConst;
import org.openimaj.rif.conditions.data.datum.RIFStringConst;
import org.openimaj.rif.conditions.data.datum.RIFUnrecognisedConst;
import org.openimaj.rif.conditions.data.datum.RIFVar;
import org.openimaj.rif.conditions.formula.RIFAnd;
import org.openimaj.rif.conditions.formula.RIFEqual;
import org.openimaj.rif.conditions.formula.RIFExists;
import org.openimaj.rif.conditions.formula.RIFFormula;
import org.openimaj.rif.conditions.formula.RIFMember;
import org.openimaj.rif.conditions.formula.RIFOr;
import org.openimaj.rif.rules.*;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.data.IStream;
import org.openimaj.squall.compile.data.jena.CombinedIVFunction;
import org.openimaj.squall.compile.data.jena.FunctorConsequence;
import org.openimaj.squall.compile.data.jena.FunctorFunction;
import org.openimaj.squall.compile.data.jena.TripleConsequence;
import org.openimaj.squall.compile.data.jena.TripleFilterFunction;
import org.openimaj.squall.compile.jena.JenaRuleCompiler;
import org.openimaj.squall.compile.rif.data.ContextConsequence;
import org.openimaj.util.data.Context;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDPlainType;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>, based on {@link JenaRuleCompiler} by Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RIFRuleCompiler implements Compiler<SourceRulesetPair> {
	
	private final class CombinedContextFunction extends
				CombinedIVFunction<Context, Context> {
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

	private void translateRuleSet(RIFRuleSet set, ContextCPS ccps){
		for (RIFSentence sentence : set){
			ContextCPS ruleret = new ContextCPS();
			List<ContextCPS> forkedCPSs = new ArrayList<ContextCPS>();
			selectCompilation(sentence,forkedCPSs);
			if (forkedCPSs.size() == 1){
				ccps.addSeperateSystem(forkedCPSs.get(0));
			}else if (forkedCPSs.size() > 1){
				ccps.addSeperateSystem(ruleret);
				for (ContextCPS fork : forkedCPSs){
					ruleret.addSeperateSystem(fork);
				}
			}
		}
	}
	
	private void selectCompilation(RIFSentence sentence, List<ContextCPS> forkedCPSs){
		CombinedContextFunction consequences = new CombinedContextFunction();
		if (sentence instanceof RIFAtomic){
			// how are we dealing with axioms?
			throw new UnsupportedOperationException("RIF translation: Axioms are currently unsupported.");
		} else {
			if (sentence instanceof RIFGroup){
				translate((RIFGroup) sentence, forkedCPSs);
			} else if (sentence instanceof RIFForAll){
				translate((RIFForAll) sentence, forkedCPSs, consequences);
			} else if (sentence instanceof RIFRule){
				translate((RIFRule) sentence, forkedCPSs, consequences);
			}
		}
	}
	
	private void translate(RIFGroup g, List<ContextCPS> forkedCPSs){
		for (RIFSentence sentence : g){
			ContextCPS ruleret = new ContextCPS();
			List<ContextCPS> newlyForkedCPSs = new ArrayList<ContextCPS>();
			selectCompilation(sentence,newlyForkedCPSs);
			if (newlyForkedCPSs.size() == 1){
				forkedCPSs.add(newlyForkedCPSs.get(0));
			}else if (forkedCPSs.size() > 1){
				forkedCPSs.add(ruleret);
				for (ContextCPS fork : forkedCPSs){
					ruleret.addSeperateSystem(fork);
				}
			}
		}
	}
	
	private void translate(RIFForAll fa, List<ContextCPS> forkedCPSs, CombinedContextFunction consequences){
		if (fa.getStatement() instanceof RIFAtomic){
			// how are we dealing with variable axioms???
			throw new UnsupportedOperationException("RIF translation: Universal facts are currently unsupported.");
		} else if (fa.getStatement() instanceof RIFRule) {
			translate((RIFRule) fa.getStatement(), forkedCPSs, consequences); 
		}
		consequences.addFunction(new ContextConsequence());
		for (ContextCPS ccps : forkedCPSs){
			// TODO 
		}
	}
	
	private void translate(RIFRule r, List<ContextCPS> forkedCPSs, CombinedContextFunction consequences){
		Count varCount = new Count(0);
		Map<String,Integer> bindingIndecies = new HashMap<String,Integer>();
		
		RIFFormula formula = r.getBody();
		
		translate(r.getBody(), forkedCPSs, bindingIndecies, varCount);
		
		Node_RuleVariable[] ruleVars = new Node_RuleVariable[bindingIndecies.size()];
		for (String name : bindingIndecies.keySet()){
			int index = bindingIndecies.get(name);
			ruleVars[index] = new Node_RuleVariable(name,index);
		}
		
		if (ccps.getConequences() != null)
			consequences.addFunction(ccps.getConequences());
		for (RIFAtomic atomic : r.head()){
			List<TriplePattern> triples = translate(atomic, ccps, bindingIndecies, varCount);
			for (TriplePattern tp : triples){
				consequences.addFunction(new TripleConsequence(ruleVars,tp));
			}
		}
		ccps.setConsequence(consequences);
		for (ContextCPS fccps : forkedCPSs){
			fccps.setConsequence(consequences);
		}
	}
	
	private void translate(RIFFormula formula, List<ContextCPS> forkedCPSs, Map<String,Integer> bindingIndecies, Count varCount){
		if (formula instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) formula, ccps, bindingIndecies, varCount);
			for (TriplePattern tp : triples){
				for (ContextCPS fccps : forkedCPSs){
					fccps.addFilter(new TripleFilterFunction(tp));
				}
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula){
				translate(f, forkedCPSs, bindingIndecies, varCount);
			}
		} else if (formula instanceof RIFOr){
			List<ContextCPS> newlyForkedCPSs = new ArrayList<ContextCPS>();
			for (RIFFormula f : (RIFOr) formula){
				List<ContextCPS> newForks = new ArrayList<ContextCPS>();
				for (ContextCPS ccps : forkedCPSs)
					newForks.add(ccps.clone());
				
				newlyForkedCPSs.add(newCCPS);
				newlyForkedCPSs.addAll(newForks);
			}
		} else if (formula instanceof RIFMember){
			
		} else if (formula instanceof RIFEqual){
			
		} else if (formula instanceof RIFExists){
			
		} else {
			
		}
	}
	
	private List<TriplePattern> translate(RIFAtomic atomic, ContextCPS ccps, Map<String,Integer> bindingIndecies, Count varCount){
		if (atomic instanceof RIFAtom){
			// how are we handling predicate logic? Named Tuples?
			throw new UnsupportedOperationException("RIF translation: Currently no support for atoms.");
		} else if (atomic instanceof RIFFrame) {
			RIFFrame frame = (RIFFrame) atomic;
			Node subject = translate(frame.getSubject(),bindingIndecies,varCount);
			for (int i = 0; i < frame.getPredicateObjectPairCount(); i++){
				
			}
		}
		throw new UnsupportedOperationException("RIF translation: Unrecognised atomic type.");
	}
	
	private Node translate(RIFDatum datum, Map<String, Integer> bindingIndecies, Count varCount){
		if (datum instanceof RIFVar) {
			String name = ((RIFVar) datum).getName();
			int i;
			if (bindingIndecies.get(name) != null)
				i = bindingIndecies.get(name);
			else {
				i = varCount.inc();
				bindingIndecies.put(name, i);
			}
			return new Node_RuleVariable(name, i);
		}
		if (datum instanceof RIFConst) {
			return translate((RIFConst<?>) datum);
		}
		if (datum instanceof RIFExternal){
			// TODO account for externals
			throw new UnsupportedOperationException("RIF translation: Currently no support for Externals.");
		}
		throw new UnsupportedOperationException("RIF translation: Unrecognised datum type.");
	}
	
	private Node translate(RIFConst<?> datum){
		String val = datum.getData().toString();
		
		if (datum instanceof RIFIRIConst) {
			return Node.createURI(val);
		}
		if (datum instanceof RIFStringConst){
			return Node.createLiteral(val, XSDDatatype.XSDstring);
		}
		return Node.createLiteral(val, new BaseDatatype(datum.getDatatype()));
	}
	
	@Override
	public CompiledProductionSystem compile(SourceRulesetPair sourceRules) {
		// Extract Sources and Rule Sets from the input.
		RIFRuleSet ruleSet = sourceRules.secondObject();
		List<IStream<Context>> sources = sourceRules.firstObject();
		// Create a Context-based compiled production system
		ContextCPS ret = new ContextCPS();
		// Add all sources to the compiled production system
		for (IStream<Context> stream : sources) {
			ret.addSource(stream);
		}
		
		translateRuleSet(ruleSet,ret);
		
		for (Rule rule : rules) {
			if (rule.isAxiom()) {
				// how are we dealing with axioms?
			}
			else
			{
				ContextCPS ruleret = new ContextCPS();
				ret.addSeperateSystem(ruleret);

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
				ruleret.setConsequence(comb);
			}
		}
		return ret;
	}

}
