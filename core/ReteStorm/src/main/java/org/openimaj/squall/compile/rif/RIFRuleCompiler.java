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
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.compile.data.jena.CombinedIVFunction;
import org.openimaj.squall.compile.data.jena.FunctorConsequence;
import org.openimaj.squall.compile.data.jena.FunctorFunction;
import org.openimaj.squall.compile.data.jena.TripleConsequence;
import org.openimaj.squall.compile.data.jena.TripleFilterFunction;
import org.openimaj.squall.compile.jena.JenaRuleCompiler;
import org.openimaj.squall.compile.rif.data.BindingConsequence;
import org.openimaj.squall.compile.rif.data.PredicateEqualityFunction;
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
			ccps.addSystem(ruleret);
			selectCompilation(sentence,ruleret);
		}
	}
	
	private void selectCompilation(RIFSentence sentence, ContextCPS ccps){
		if (sentence instanceof RIFAtomic){
			// how are we dealing with axioms?
			throw new UnsupportedOperationException("RIF translation: Axioms are currently unsupported.");
		} else {
			if (sentence instanceof RIFGroup){
				translate((RIFGroup) sentence, ccps);
			} else if (sentence instanceof RIFForAll){
				translate((RIFForAll) sentence, ccps);
			} else if (sentence instanceof RIFRule){
				translate((RIFRule) sentence, ccps);
			}
		}
	}
	
	private void translate(RIFGroup g, ContextCPS ccps){
		for (RIFSentence sentence : g){
			ContextCPS ruleret = new ContextCPS();
			ccps.addSystem(ruleret);
			selectCompilation(sentence,ruleret);
		}
	}
	
	private void translate(RIFForAll fa, ContextCPS ccps){
		if (fa.getStatement() instanceof RIFAtomic){
			// how are we dealing with variable axioms???
			throw new UnsupportedOperationException("RIF translation: Universal facts are currently unsupported.");
		} else if (fa.getStatement() instanceof RIFRule) {
			translate((RIFRule) fa.getStatement(), ccps); 
		}
		
		IVFunction<Context,Context> consequence = new BindingConsequence();
		if (ccps.getConequences() != null){
			CombinedContextFunction consequences = new CombinedContextFunction();
			consequences.addFunction(ccps.getConequences());
			consequences.addFunction(consequence);
			consequence = consequences;
		}
		ccps.setConsequence(consequence);
	}
	
	private void translate(RIFRule r, ContextCPS ccps){
		Count varCount = new Count(0);
		Map<String,Integer> bindingIndecies = new HashMap<String,Integer>();
		
		translate(r.getBody(), ccps, bindingIndecies, varCount);
		
		Node_RuleVariable[] ruleVars = new Node_RuleVariable[bindingIndecies.size()];
		for (String name : bindingIndecies.keySet()){
			int index = bindingIndecies.get(name);
			ruleVars[index] = new Node_RuleVariable(name,index);
		}
		
		CombinedContextFunction consequences = new CombinedContextFunction();
		if (ccps.getConequences() != null)
			consequences.addFunction(ccps.getConequences());
		for (RIFAtomic atomic : r.head()){
			List<TriplePattern> triples = translate(atomic, ccps, bindingIndecies, varCount);
			for (TriplePattern tp : triples){
				consequences.addFunction(new TripleConsequence());
			}
		}
		ccps.setConsequence(consequences);
	}
	
	private void translate(RIFFormula formula, ContextCPS ccps, Map<String,Integer> bindingIndecies, Count varCount){
		if (formula instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) formula, ccps, bindingIndecies, varCount);
			for (TriplePattern tp : triples){
					ccps.addJoinComponent(new TripleFilterFunction(tp));
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula){
				translate(f, ccps, bindingIndecies, varCount);
			}
		} else if (formula instanceof RIFOr){
			for (RIFFormula f : (RIFOr) formula){
				ContextCPS ruleret = new ContextCPS();
				ccps.addSystem(ruleret);
				translate(f,ruleret, bindingIndecies, varCount);
			}
		} else if (formula instanceof RIFMember){
			RIFMember member = (RIFMember) formula;
			ccps.addJoinComponent(
				new TripleFilterFunction(
					new TriplePattern(
						translate(member.getInstance(),bindingIndecies,varCount),
						Node.createURI(""/*TODO set as rdfs:typeOf*/),
						translate(member.getInClass(),bindingIndecies,varCount)
					)
				)
			);
		} else if (formula instanceof RIFEqual){
			RIFEqual equal = (RIFEqual) formula;
			Node[] equalData = new Node[2];
			equalData[0] = translate(equal.getLeft(),bindingIndecies,varCount);
			equalData[1] = translate(equal.getRight(),bindingIndecies,varCount);
			ccps.addPredicate(
				new PredicateEqualityFunction(
					equalData
				)
			);
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
		
		return ret;
	}

}
