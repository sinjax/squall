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
import org.openimaj.rif.conditions.data.RIFList;
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
		
		// Create a list to contain all the vars stated in the production system.
		List<Node_RuleVariable> vars = new ArrayList<Node_RuleVariable>();
		
		for (RIFGroup g : ruleSet)
			translate(g, ret, vars);
		
		return ret;
	} 

	protected void selectCompilation(RIFSentence sentence, ContextCPS ccps, List<Node_RuleVariable> vars) throws RIFPredicateException, UnsupportedOperationException {
		if (sentence instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) sentence, ccps, vars);
			// TODO
			// throw new UnsupportedOperationException("RIF translation: Axioms are currently unsupported.");
		} else if (sentence instanceof RIFRule){
			translate((RIFRule) sentence, ccps, vars);
		} else if (sentence instanceof RIFGroup){
			translate((RIFGroup) sentence, ccps, vars);
		} else if (sentence instanceof RIFForAll){
			translate((RIFForAll) sentence, ccps);
		} else {
			throw new UnsupportedOperationException("RIF translation: Unrecognised extension of RIFSentence: "+sentence.getClass().getName());
		}
	}
	
	protected void translate(RIFGroup g, ContextCPS ccps, List<Node_RuleVariable> vars) throws UnsupportedOperationException {
		for (RIFSentence sentence : g){
			try {
				ContextCPS ruleret = new ContextCPS();
				selectCompilation(sentence,ruleret, vars);
				ccps.addSystem(ruleret);
			} catch (RIFPredicateException e) {
				System.err.println("Incorrect function specification in the following rule with the following message:\n\t"+e.getMessage()+"\n"+sentence.toString());
			}
		}
	}
	
	/**
	 * Adds the proffered node to the proffered list of variable nodes if:
	 * <ul>
	 * <il>the node is a variable AND</il>
	 * <il>the node does not already exist in the list AND</il>
	 * <il>the node does not share a name with another node in the list.</il>
	 * </ul>
	 * Returns the proffered node in all cases.
	 * @param var
	 * @param vars
	 * @return
	 */
	private Node addNodeUniquelyToVarList(Node var, List<Node_RuleVariable> vars){
		if (var.isVariable())
			if (!vars.contains(var)){
				for (Node_RuleVariable v : vars)
					if (v.getName().equals(var.getName())) return var;
				vars.add((Node_RuleVariable) var);
			}
		return var;
	}
	
	protected void translate(RIFForAll fa, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		List<Node_RuleVariable> vars = new ArrayList<Node_RuleVariable>();
		for (RIFVar v : fa.universalVars())
			vars.add(v.getNode());
		
		if (fa.getStatement() instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) fa.getStatement(), ccps, vars);
			// TODO
			throw new UnsupportedOperationException("RIF translation: Universal facts are currently unsupported.");
		} else if (fa.getStatement() instanceof RIFRule) {
			translate((RIFRule) fa.getStatement(), ccps, vars); 
		}
		
		IVFunction<Context,Context> newConsequence = new RIFForAllBindingConsequence(fa);
		IVFunction<Context, Context> consequence = ccps.getConsequence();
		if (consequence != null){
			MultiConsequence consequences;
			if (consequence instanceof MultiConsequence){
				consequences = (MultiConsequence) consequence;
			} else {
				consequences = new MultiConsequence(consequence);
			}
			consequences.addFunction(newConsequence);
			consequence = consequences;
		}
		ccps.setConsequence(consequence);
	}
	
	protected void translate(RIFRule r, ContextCPS ccps, List<Node_RuleVariable> vars) throws RIFPredicateException, UnsupportedOperationException {
		List<Node_RuleVariable> newVars = translateBody(r.getBody(), ccps);
		for (Node_RuleVariable var : newVars)
			addNodeUniquelyToVarList(var, vars);
		
		IVFunction<Context, Context> consequence = ccps.getConsequence();
		MultiConsequence consequences;
		if (consequence != null){
			if (consequence instanceof MultiConsequence){
				consequences = (MultiConsequence) consequence;
			} else {
				consequences = new MultiConsequence(consequence);
			}
		} else {
			consequences = new MultiConsequence();
		}
		translateHead(r.getHead(), ccps, consequences);
		ccps.setConsequence(consequences);
	}
	
	protected List<Node_RuleVariable> translateBody(RIFFormula formula, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		List<Node_RuleVariable> vars = new ArrayList<Node_RuleVariable>();
		if (formula instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) formula, ccps, vars);
			for (TriplePattern tp : triples){
					ccps.addJoinComponent(new BaseTripleFilterFunction(tp));
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula){
				List<Node_RuleVariable> newVars = translateBody(f, ccps);
				for (Node_RuleVariable var : newVars)
					addNodeUniquelyToVarList(var, vars);
			}
		} else if (formula instanceof RIFOr){
			for (RIFFormula f : (RIFOr) formula){
				ContextCPS ruleret = new ContextCPS();
				List<Node_RuleVariable> newVars = translateBody(f,ruleret);
				
				ruleret.setConsequence(
						new BaseBindingConsequence(
								newVars
						)
				);
				ccps.addSystem(ruleret);
				
				for (Node_RuleVariable var : newVars)
					addNodeUniquelyToVarList(var, vars);
			}
		} else if (formula instanceof RIFMember){
			RIFMember member = (RIFMember) formula;
			addNodeUniquelyToVarList(translate(member.getInstance(),ccps), vars);
			addNodeUniquelyToVarList(translate(member.getInClass(),ccps), vars);
			ccps.addJoinComponent(
				new RIFMemberFilterFunction(member)
			);
		} else if (formula instanceof RIFEqual){
			RIFEqual equal = (RIFEqual) formula;
			addNodeUniquelyToVarList(translate(equal.getLeft(),ccps), vars);
			addNodeUniquelyToVarList(translate(equal.getRight(),ccps), vars);
			ccps.addPredicate(
				new RIFCorePredicateEqualityFunction(equal)
			);
		} else if (formula instanceof RIFExists){
			List<Node_RuleVariable> newVars = translateBody(((RIFExists) formula).getFormula(), ccps);
			for (Node_RuleVariable var : newVars)
				if (!((RIFExists) formula).containsExistentialVar(var.getName()))
					addNodeUniquelyToVarList(var, vars);
		} else if (formula instanceof RIFExternalValue) {
			UnsupportedOperationException error = null; 
			RIFExternalValue val = (RIFExternalValue) formula;
			for (RIFExternalFunctionLibrary lib : this.externalLibs) try {
				ccps.addPredicate(lib.compile(val));
				return vars;
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
		
		return vars;
	}
	
	protected List<Node_RuleVariable> translateHead(RIFFormula formula, ContextCPS ccps, MultiConsequence mc) throws RIFPredicateException, UnsupportedOperationException {
		List<Node_RuleVariable> vars = new ArrayList<Node_RuleVariable>();
		if (formula instanceof RIFAtomic){
			List<TriplePattern> triples = translate((RIFAtomic) formula, ccps, vars);
			for (TriplePattern tp : triples){
					mc.addFunction(new BaseTripleConsequence(tp));
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula)
				for (Node_RuleVariable var : translateHead(f, ccps, mc))
					addNodeUniquelyToVarList(var, vars);
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
		return vars;
	}
	
	protected List<TriplePattern> translate(RIFAtomic atomic, ContextCPS ccps, List<Node_RuleVariable> vars) throws UnsupportedOperationException {
		List<TriplePattern> triples = new ArrayList<TriplePattern>();
		if (atomic instanceof RIFAtom){
			RIFAtom atom = (RIFAtom) atomic;
			Node subject, predicate, object;
			switch (atom.getArgsSize()){
				case 1:
					object = addNodeUniquelyToVarList(translate(atom.getOp(), ccps), vars);
					predicate = Node.createURI(RIFMemberFilterFunction.RDF_TYPE_URI);
					break;
				case 2:
					object = addNodeUniquelyToVarList(translate(atom.getArg(1), ccps), vars);
					predicate = addNodeUniquelyToVarList(translate(atom.getOp(), ccps), vars);
					break;
				case 3:
					if (atom.getOp().getNode().getLiteralValue().equals("triple")){
						object = addNodeUniquelyToVarList(translate(atom.getArg(2), ccps), vars);
						predicate = addNodeUniquelyToVarList(translate(atom.getArg(1), ccps), vars);
						break;
					}
				default:
					throw new UnsupportedOperationException("RIF translation: only 'Atom' elements denoting triples are currently supported:"
																			+"\nObject(Subject) = Subject rdf:type Object"
																			+"\nPredicate(Subject, Object) = Subject Predicate Object"
																			+"\ntriple(Subject, Predicate, Object) = Subject, Predicate, Object");
			}
			subject = addNodeUniquelyToVarList(translate(atom.getArg(0), ccps), vars);
			triples.add(new TriplePattern(subject, predicate, object));
		} else if (atomic instanceof RIFFrame) {
			RIFFrame frame = (RIFFrame) atomic;
			Node subject = addNodeUniquelyToVarList(translate(frame.getSubject(), ccps), vars);
			for (int i = 0; i < frame.getPredicateObjectPairCount(); i++){
				Node predicate = addNodeUniquelyToVarList(translate(frame.getPredicate(i), ccps), vars);
				Node object = addNodeUniquelyToVarList(translate(frame.getObject(i), ccps), vars);
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
		} else if (data instanceof RIFList){ // Handle Lists
			throw new UnsupportedOperationException("RIF translation: Currently no support for RIF Lists.  RDF based List patterns must be explicitly described in order to match the specific list implementation in the instance data.");
		} else { // Handle Lists
			throw new UnsupportedOperationException("RIF translation: Unrecognised extension of RIFData: "+data);
		}
	}

}