package org.openimaj.squall.compile.rif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.rif.*;
import org.openimaj.rif.conditions.*;
import org.openimaj.rif.conditions.atomic.*;
import org.openimaj.rif.conditions.data.RIFConst;
import org.openimaj.rif.conditions.data.RIFDatum;
import org.openimaj.rif.conditions.data.RIFExpr;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.data.RIFFunction;
import org.openimaj.rif.conditions.data.RIFIRIConst;
import org.openimaj.rif.conditions.data.RIFStringConst;
import org.openimaj.rif.conditions.data.RIFTypedConst;
import org.openimaj.rif.conditions.data.RIFVar;
import org.openimaj.rif.conditions.formula.RIFAnd;
import org.openimaj.rif.conditions.formula.RIFEqual;
import org.openimaj.rif.conditions.formula.RIFExists;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.rif.conditions.formula.RIFFormula;
import org.openimaj.rif.conditions.formula.RIFMember;
import org.openimaj.rif.conditions.formula.RIFOr;
import org.openimaj.rif.rules.*;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.data.IVFunction;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.functions.rif.RIFExprLibrary;
import org.openimaj.squall.functions.rif.RIFExternalFunctionLibrary;
import org.openimaj.squall.functions.rif.consequences.BindingConsequence;
import org.openimaj.squall.functions.rif.consequences.MultiConsequence;
import org.openimaj.squall.functions.rif.consequences.TripleConsequence;
import org.openimaj.squall.functions.rif.core.RIFCoreExprLibrary;
import org.openimaj.squall.functions.rif.core.RIFCorePredicateEqualityFunction;
import org.openimaj.squall.functions.rif.filters.BaseTripleFilterFunction;
import org.openimaj.squall.functions.rif.filters.RIFMemberFilterFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateEqualityFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

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
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFCoreRuleCompiler implements Compiler<SourceRulesetLibsTrio> {
	
	// Set the default RIF Expr library:
	private static final RIFExprLibrary RIF_LIB = new RIFCoreExprLibrary();
	
	private List<RIFExternalFunctionLibrary> externalLibs;

	protected void selectCompilation(RIFSentence sentence, ContextCPS ccps) throws RIFPredicateException {
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
	
	protected void translate(RIFGroup g, ContextCPS ccps){
		for (RIFSentence sentence : g){
			try {
				ContextCPS ruleret = new ContextCPS();
				selectCompilation(sentence,ruleret);
				ccps.addSystem(ruleret);
			} catch (RIFPredicateException e) {
				System.err.println("Incorrect function specification in the following rule with the following message:\n\t"+e.getMessage()+"\n"+sentence.toString());
			}
		}
	}
	
	protected void translate(RIFForAll fa, ContextCPS ccps) throws RIFPredicateException {
		if (fa.getStatement() instanceof RIFAtomic){
			// how are we dealing with variable axioms???
			throw new UnsupportedOperationException("RIF translation: Universal facts are currently unsupported.");
		} else if (fa.getStatement() instanceof RIFRule) {
			translate((RIFRule) fa.getStatement(), ccps); 
		}
		
		List<Node_RuleVariable> vars = new ArrayList<Node_RuleVariable>();
		for (RIFVar var : fa.universalVars()) vars.add(var.getNode());
		IVFunction<Context,Context> consequence = new BindingConsequence(vars);
		if (ccps.getConequences() != null){
			MultiConsequence consequences = new MultiConsequence(ccps.getConequences());
			consequences.addFunction(consequence);
			consequence = consequences;
		}
		ccps.setConsequence(consequence);
	}
	
	protected void translate(RIFRule r, ContextCPS ccps) throws RIFPredicateException {
		translateBody(r.getBody(), ccps);
		
		MultiConsequence consequences;
		if (ccps.getConequences() != null)
			consequences = new MultiConsequence(ccps.getConequences());
		else
			consequences = new MultiConsequence();
		translateHead(r.getHead(), ccps, consequences);
		ccps.setConsequence(consequences);
	}
	
	protected void translateBody(RIFFormula formula, ContextCPS ccps) throws RIFPredicateException {
		if (formula instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) formula, ccps);
			for (TriplePattern tp : triples){
				// FIXME
//					ccps.addJoinComponent(new BaseTripleFilterFunction(tp));
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula){
				translateBody(f, ccps);
			}
		} else if (formula instanceof RIFOr){
			for (RIFFormula f : (RIFOr) formula){
				ContextCPS ruleret = new ContextCPS();
				translateBody(f,ruleret);
				ccps.addSystem(ruleret);
			}
		} else if (formula instanceof RIFMember){
			RIFMember member = (RIFMember) formula;
			ccps.addJoinComponent(
				new RIFMemberFilterFunction(member)
			);
		} else if (formula instanceof RIFEqual){
			RIFEqual equal = (RIFEqual) formula;
			ccps.addPredicate(
				new RIFCorePredicateEqualityFunction(equal)
			);
		} else if (formula instanceof RIFExists){
			translateBody(((RIFExists) formula).getFormula(), ccps);
		} else if (formula instanceof RIFExternalValue) {
			UnsupportedOperationException error = null; 
			for (RIFExternalFunctionLibrary lib : this.externalLibs) try {
				RIFExternalValue val = (RIFExternalValue) formula;
				ccps.addPredicate(lib.compile(val));
				return;
			} catch (UnsupportedOperationException e) {
				if (error == null) error = e;
				else error.addSuppressed(e);
			}
			if (error == null) throw new UnsupportedOperationException("RIF translation: No external function libraries provided.");
			throw error;
		} else {
			throw new UnsupportedOperationException("RIF translation: Unrecognised formula expression type.");
		}
	}
	
	protected void translateHead(RIFFormula formula, ContextCPS ccps, MultiConsequence mc) throws RIFPredicateException {
		if (formula instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) formula, ccps);
			for (TriplePattern tp : triples){
					mc.addFunction(new TripleConsequence(tp));
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula){
				translateHead(f, ccps, mc);
			}
		} else if (formula instanceof RIFOr){
			throw new UnsupportedOperationException("RIF-Core translation: Disjunctive statements in rule heads are not supported in RIF Core.");
		} else if (formula instanceof RIFMember){
			throw new UnsupportedOperationException("RIF-Core translation: Membership statements in rule heads are not supported in RIF Core.");
		} else if (formula instanceof RIFEqual){
			throw new UnsupportedOperationException("RIF-Core translation: Equality statements in rule heads are not supported in RIF Core.");
		} else if (formula instanceof RIFExists){
			throw new UnsupportedOperationException("RIF-Core translation: Existential statements in rule heads are not supported in RIF Core.");
		} else if (formula instanceof RIFExternalValue) {
			throw new UnsupportedOperationException("RIF-Core translation: External predicates in rule heads are not supported in RIF Core.");
		} else {
			throw new UnsupportedOperationException("RIF translation: Unrecognised formula expression type.");
		}
	}
	
	protected List<TriplePattern> translate(RIFAtomic atomic, ContextCPS ccps){
		List<TriplePattern> triples = new ArrayList<TriplePattern>();
		if (atomic instanceof RIFAtom){
			// how are we handling predicate logic? Named Tuples?
			throw new UnsupportedOperationException("RIF translation: Currently no support for atoms.");
		} else if (atomic instanceof RIFFrame) {
			RIFFrame frame = (RIFFrame) atomic;
			Node subject = translate(frame.getSubject(), ccps);
			for (int i = 0; i < frame.getPredicateObjectPairCount(); i++){
				Node predicate = translate(frame.getPredicate(i), ccps);
				Node object = translate(frame.getObject(i), ccps);
				triples.add(new TriplePattern(subject, predicate, object));
			}
		}
		throw new UnsupportedOperationException("RIF translation: Unrecognised atomic type.");
	}
	
	protected Node translate(RIFDatum datum, ContextCPS ccps){
		Node ret = datum.getNode();
		if (datum instanceof RIFFunction){
			if (datum instanceof RIFExpr){
				RIFExpr expr = (RIFExpr) datum;
				ccps.addPredicate(RIF_LIB.compile(expr));
				return ret;
			} else if (datum instanceof RIFExternalExpr){
				UnsupportedOperationException error = null; 
				for (RIFExternalFunctionLibrary lib : this.externalLibs) try {
					RIFExternalExpr expr = (RIFExternalExpr) datum;
					ccps.addPredicate(lib.compile(expr));
					return ret;
				} catch (UnsupportedOperationException e) {
					if (error == null) error = e;
					else error.addSuppressed(e);
				}
				if (error == null) throw new UnsupportedOperationException("RIF translation: No external function libraries provided.");
				throw error;
			} else {
				throw new UnsupportedOperationException("RIF translation: Currently no support for functions other than Expr and External Expr.");
			}
		}
		return ret;
	}
	
	
	
	@Override
	public CompiledProductionSystem compile(SourceRulesetLibsTrio sourceRules) {
		// Extract Sources, Rule Sets and External Libraries from the input.
		RIFRuleSet ruleSet = sourceRules.secondObject();
		List<ISource<Stream<Context>>> sources = sourceRules.firstObject();
		this.externalLibs = sourceRules.thirdObject();
		// Create a Context-based compiled production system
		ContextCPS ret = new ContextCPS();
		// Add all sources to the compiled production system
		for (ISource<Stream<Context>> stream : sources) {
			ret.addSource(stream);
		}
		
		try {
			ContextCPS ruleret = new ContextCPS();
			selectCompilation(ruleSet.getRootGroup(),ruleret);
			ret.addSystem(ruleret);
		} catch (RIFPredicateException e) {
			System.err.println("Incorrect function specification in the following rule with the following message:\n\t"
									+e.getMessage()+"\n"+ruleSet.getRootGroup().toString());
		}
		
		return ret;
	}

}