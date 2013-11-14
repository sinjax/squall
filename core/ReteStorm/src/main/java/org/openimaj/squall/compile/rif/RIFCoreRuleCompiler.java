package org.openimaj.squall.compile.rif;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.openimaj.rif.*;
import org.openimaj.rif.conditions.atomic.*;
import org.openimaj.rif.conditions.data.RIFData;
import org.openimaj.rif.conditions.data.RIFDatum;
import org.openimaj.rif.conditions.data.RIFExpr;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.data.RIFFunction;
import org.openimaj.rif.conditions.data.RIFIRIConst;
import org.openimaj.rif.conditions.data.RIFList;
import org.openimaj.rif.conditions.data.RIFLocalConst;
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
import org.openimaj.squall.functions.rif.consequences.BaseBindingConsequence;
import org.openimaj.squall.functions.rif.consequences.MultiConsequence;
import org.openimaj.squall.functions.rif.consequences.BaseTripleConsequence;
import org.openimaj.squall.functions.rif.core.RIFCoreExprLibrary;
import org.openimaj.squall.functions.rif.core.RIFCorePredicateEqualityFunction;
import org.openimaj.squall.functions.rif.core.RIFForAllBindingConsequence;
import org.openimaj.squall.functions.rif.core.RIFMemberFilterFunction;
import org.openimaj.squall.functions.rif.filters.BaseTripleFilterFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.squall.functions.rif.sources.RIFStreamImportProfiles;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFCoreRuleCompiler implements Compiler<RulesetLibsPair> {
	
	// Set the default RIF Expr library:
	private static final RIFExprLibrary RIF_LIB = new RIFCoreExprLibrary();
	
	private static final RIFStreamImportProfiles streamProfiles = new RIFStreamImportProfiles();
	
	private List<RIFExternalFunctionLibrary> externalLibs;
	
	@Override
	public CompiledProductionSystem compile(RulesetLibsPair sourceRules) {
		// Create a Context-based compiled production system
		ContextCPS ret = new ContextCPS();
		
		// Extract Rule Sets and External Libraries from the input.
		RIFRuleSet ruleSet = sourceRules.firstObject();
		this.externalLibs = sourceRules.secondObject();
		// Add sources to compiled production system from Rule Set
		for (URI uri : ruleSet.getImportKeySet()){
			if (RIFCoreRuleCompiler.streamProfiles.containsKey(
					ruleSet.getImport(uri)
				)){
				ret.addSource(
						RIFCoreRuleCompiler.streamProfiles.get(
								ruleSet.getImport(uri)
						).createSource(uri)
				);
			}
		}
		
		for (RIFGroup g : ruleSet)
			translate(g, ret);
		
		return ret;
	} 

	protected void selectCompilation(RIFSentence sentence, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		if (sentence instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) sentence, ccps);
			// TODO
			// throw new UnsupportedOperationException("RIF translation: Axioms are currently unsupported.");
		} else if (sentence instanceof RIFRule){
			translate((RIFRule) sentence, ccps);
		} else if (sentence instanceof RIFGroup){
			translate((RIFGroup) sentence, ccps);
		} else if (sentence instanceof RIFForAll){
			translate((RIFForAll) sentence, ccps);
		} else {
			throw new UnsupportedOperationException("RIF translation: Unrecognised extension of RIFSentence: "+sentence.getClass().getName());
		}
	}
	
	protected void translate(RIFGroup g, ContextCPS ccps) throws UnsupportedOperationException {
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
	
	protected void translate(RIFForAll fa, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		if (fa.getStatement() instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) fa.getStatement(), ccps);
			// TODO
			throw new UnsupportedOperationException("RIF translation: Universal facts are currently unsupported.");
		} else if (fa.getStatement() instanceof RIFRule) {
			translate((RIFRule) fa.getStatement(), ccps); 
		}
		
		ccps.addConsequence(new RIFForAllBindingConsequence(fa));
	}
	
	protected void translate(RIFRule r, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		translateBody(r.getBody(), ccps);
		translateHead(r.getHead(), ccps);
	}
	
	protected void translateBody(RIFFormula formula, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		if (formula instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) formula, ccps);
			for (TriplePattern tp : triples){
					ccps.addJoinComponent(new BaseTripleFilterFunction(tp));
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
			translate(member.getInstance(),ccps);
			translate(member.getInClass(),ccps);
			ccps.addJoinComponent(
				new RIFMemberFilterFunction(member)
			);
		} else if (formula instanceof RIFEqual){
			RIFEqual equal = (RIFEqual) formula;
			translate(equal.getLeft(),ccps);
			translate(equal.getRight(),ccps);
			ccps.addPredicate(
				new RIFCorePredicateEqualityFunction(equal)
			);
		} else if (formula instanceof RIFExists){
			translateBody(((RIFExists) formula).getFormula(), ccps);
		} else if (formula instanceof RIFExternalValue) {
			UnsupportedOperationException error = null; 
			RIFExternalValue val = (RIFExternalValue) formula;
			for (RIFExternalFunctionLibrary lib : this.externalLibs) try {
				ccps.addPredicate(lib.compile(val));
				return;
			} catch (UnsupportedOperationException e) {
				if (error == null) error = e;
				else error.addSuppressed(e);
			}
// Temporary Try/Catch loop until RIFCoreExternalFunctionLibrary is implemented
try {
			if (error == null) throw new UnsupportedOperationException("RIF translation: No external function libraries provided.");
			else throw error;
} catch (UnsupportedOperationException e){
	ccps.addPredicate(new PlaceHolderExternalValueFunction(val));
}
		} else {
			throw new UnsupportedOperationException("RIF translation: Unrecognised formula expression type.");
		}
	}
	
	protected void translateHead(RIFFormula formula, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		if (formula instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) formula, ccps);
			for (TriplePattern tp : triples){
					ccps.addConsequence(new BaseTripleConsequence(tp));
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula)
				translateHead(f, ccps);
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
	
	protected List<TriplePattern> translate(RIFAtomic atomic, ContextCPS ccps) throws UnsupportedOperationException {
		List<TriplePattern> triples = new ArrayList<TriplePattern>();
		if (atomic instanceof RIFAtom){
			RIFAtom atom = (RIFAtom) atomic;
			if (atom.getOp().getDatatype().equals(RIFLocalConst.datatype)){
				
			}else if (atom.getOp().getDatatype().equals(RIFIRIConst.datatype)
					&& atom.getOp().getNode().getURI().toLowerCase().endsWith("error")) {
				return triples;
			} else {
				Node subject, predicate, object;
				switch (atom.getArgsSize()){
					case 1:
						object = translate(atom.getOp(), ccps);
						predicate = Node.createURI(RIFMemberFilterFunction.RDF_TYPE_URI);
						break;
					case 2:
						object = translate(atom.getArg(1), ccps);
						predicate = translate(atom.getOp(), ccps);
						break;
					case 3:
						if (atom.getOp().getNode().isLiteral() && atom.getOp().getNode().getLiteralValue().equals("triple")){
							object = translate(atom.getArg(2), ccps);
							predicate = translate(atom.getArg(1), ccps);
							break;
						}
					default:
						throw new UnsupportedOperationException("RIF translation: only 'Atom' elements denoting triples, local results or inconsistencies are currently supported:"
																				+"\nObject(Subject) = Subject rdf:type Object"
																				+"\nPredicate(Subject, Object) = Subject Predicate Object"
																				+"\ntriple(Subject, Predicate, Object) = Subject, Predicate, Object"
																				+"\n_name([args]) = Local Result"
																				+"\nrif:error([args]) = Inconsistency"
																);
				}
				subject = translate(atom.getArg(0), ccps);
				triples.add(new TriplePattern(subject, predicate, object));
			}
		} else if (atomic instanceof RIFFrame) {
			RIFFrame frame = (RIFFrame) atomic;
			Node subject = translate(frame.getSubject(), ccps);
			for (int i = 0; i < frame.getPredicateObjectPairCount(); i++){
				Node predicate = translate(frame.getPredicate(i), ccps);
				Node object = translate(frame.getObject(i), ccps);
				triples.add(new TriplePattern(subject, predicate, object));
			}
		} else throw new UnsupportedOperationException("RIF translation: Unrecognised atomic type.");
		
		return triples;
	}
	
	protected Node translate(RIFData data, ContextCPS ccps) throws UnsupportedOperationException {
		if (data instanceof RIFDatum){
			RIFDatum datum = (RIFDatum) data;
			Node ret = datum.getNode();
			if (datum instanceof RIFFunction){
				RIFExpr expr;
				if (datum instanceof RIFExpr){
					expr = (RIFExpr) datum;
					ccps.addPredicate(RIF_LIB.compile(expr));
					return ret;
				} else if (datum instanceof RIFExternalExpr){
					expr = ((RIFExternalExpr) datum).getExpr();
					UnsupportedOperationException error = null; 
					for (RIFExternalFunctionLibrary lib : this.externalLibs) try {
						ccps.addPredicate(lib.compile(expr));
						return ret;
					} catch (UnsupportedOperationException e) {
						if (error == null) error = e;
						else error.addSuppressed(e);
					}
try {
					if (error == null) throw new UnsupportedOperationException("RIF translation: No external function libraries provided.");
					throw error;
} catch (UnsupportedOperationException e){
	ccps.addPredicate(new PlaceHolderExprFunction(expr));
}
				} else {
					throw new UnsupportedOperationException("RIF translation: Currently no support for functions other than Expr and External Expr.");
				}
			}
			return ret;
		} else if (data instanceof RIFList){ // Handle Lists
			throw new UnsupportedOperationException("RIF translation: Currently no support for RIF Lists.  RDF based List patterns must be explicitly described in order to match the specific list implementation in the instance data.");
		} else { // Handle Lists
			throw new UnsupportedOperationException("RIF translation: Unrecognised extension of RIFData: "+data);
		}
	}

}