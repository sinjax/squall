package org.openimaj.squall.compile.rif;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.rifcore.RIFRuleSet;
import org.openimaj.rifcore.conditions.RIFExternal;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.atomic.RIFAtomic;
import org.openimaj.rifcore.conditions.atomic.RIFFrame;
import org.openimaj.rifcore.conditions.data.RIFData;
import org.openimaj.rifcore.conditions.data.RIFDatum;
import org.openimaj.rifcore.conditions.data.RIFExpr;
import org.openimaj.rifcore.conditions.data.RIFExternalExpr;
import org.openimaj.rifcore.conditions.data.RIFFunction;
import org.openimaj.rifcore.conditions.data.RIFIRIConst;
import org.openimaj.rifcore.conditions.data.RIFList;
import org.openimaj.rifcore.conditions.data.RIFLocalConst;
import org.openimaj.rifcore.conditions.formula.RIFAnd;
import org.openimaj.rifcore.conditions.formula.RIFEqual;
import org.openimaj.rifcore.conditions.formula.RIFExists;
import org.openimaj.rifcore.conditions.formula.RIFExternalValue;
import org.openimaj.rifcore.conditions.formula.RIFFormula;
import org.openimaj.rifcore.conditions.formula.RIFMember;
import org.openimaj.rifcore.conditions.formula.RIFOr;
import org.openimaj.rifcore.rules.RIFForAll;
import org.openimaj.rifcore.rules.RIFGroup;
import org.openimaj.rifcore.rules.RIFRule;
import org.openimaj.rifcore.rules.RIFSentence;
import org.openimaj.squall.compile.CompiledProductionSystem;
import org.openimaj.squall.compile.Compiler;
import org.openimaj.squall.compile.ContextCPS;
import org.openimaj.squall.compile.data.source.URIProfileISourceFactory;
import org.openimaj.squall.compile.rif.provider.ExternalFunctionRegistry;
import org.openimaj.squall.compile.OptionalProductionSystems;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.functions.rif.RIFExprLibrary;
import org.openimaj.squall.functions.rif.consequences.RIFAtomConsequence;
import org.openimaj.squall.functions.rif.consequences.RIFTripleConsequence;
import org.openimaj.squall.functions.rif.core.RIFCoreExprLibrary;
import org.openimaj.squall.functions.rif.core.RIFCorePredicateEqualityFunction;
import org.openimaj.squall.functions.rif.core.RIFMemberFilterFunction;
import org.openimaj.squall.functions.rif.filters.BaseAtomFilterFunction;
import org.openimaj.squall.functions.rif.filters.BaseTripleFilterFunction;
import org.openimaj.squall.functions.rif.predicates.BaseRIFPredicateFunction.RIFPredicateException;
import org.openimaj.squall.providers.rif.consequences.RIFForAllBindingConsequenceProvider;
import org.openimaj.squall.util.MD5Utils;
import org.openimaj.util.data.Context;
import org.openimaj.util.stream.Stream;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFCoreRuleCompiler implements Compiler<RIFRuleSet> {
	
	// Set the default RIF Expr library:
	private static final RIFExprLibrary RIF_LIB = new RIFCoreExprLibrary();
	private static final Logger logger = Logger.getLogger(RIFCoreRuleCompiler.class);
	
	@Override
	public CompiledProductionSystem compile(RIFRuleSet ruleSet) {
		// Create a Context-based compiled production system
		ContextCPS ret = new ContextCPS();
		
		// Add sources to compiled production system from Rule Set
		for (URI uri : ruleSet.getImportKeySet()){
			try{
				ISource<Stream<Context>> source = URIProfileISourceFactory.instance().createSource(uri, ruleSet.getImport(uri));
				ret.addStreamSource(source);
			} catch (UnsupportedOperationException e) {
				logger.debug(String.format("Could not process import at uri <%s> with profile <%s>.", uri, ruleSet.getImport(uri)), e);
			}
		}
		
		for (RIFGroup g : ruleSet)
			translate(g, ret);
		
		return ret;
	} 

	protected void selectCompilation(RIFSentence sentence, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		if (sentence instanceof RIFAtomic){
			ccps.addAxioms(translate((RIFAtomic) sentence, ccps));
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
		OptionalProductionSystems options = new OptionalProductionSystems();
		for (RIFSentence sentence : g){
			try {
				ContextCPS ruleret = new ContextCPS();
				selectCompilation(sentence,ruleret);
				options.add(ruleret);
			} catch (RIFPredicateException e) {
				System.err.println("Incorrect function specification in the following rule with the following message:\n\t"+e.getMessage()+"\n"+sentence.toString());
			}
		}
		ccps.addOption(options);
	}
	
	protected void translate(RIFForAll fa, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		if (fa.getStatement() instanceof RIFAtomic){
			ccps.addAxioms(translate((RIFAtomic) fa.getStatement(), ccps));
		} else if (fa.getStatement() instanceof RIFRule) {
			if (fa.getStatement().getID() == null)
				fa.getStatement().setID(fa.getID());
			translate((RIFRule) fa.getStatement(), ccps); 
		}
		
		RIFForAllBindingConsequenceProvider consequenceProvider;
		if (fa.getID() == null)
			consequenceProvider = new RIFForAllBindingConsequenceProvider(MD5Utils.md5Hex(fa.toString()));
		else
			consequenceProvider = new RIFForAllBindingConsequenceProvider(fa.getID().getNode().getURI());
		ccps.addConsequence(consequenceProvider.apply(fa));
	}
	
	protected void translate(RIFRule r, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		translateBody(r.getBody(), ccps);
		String id;
		if (r.getID() == null)
			id = MD5Utils.md5Hex(r.toString());
		else
			id = r.getID().getNode().getURI();
		translateHead(id, r.getHead(), ccps);
	}
	
	protected void translateBody(RIFFormula formula, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		if (formula instanceof RIFAtomic){
			List<ClauseEntry> triples = translate((RIFAtomic) formula, ccps);
			for (ClauseEntry tp : triples){
				if (tp instanceof TriplePattern)
					ccps.addJoinComponent(new BaseTripleFilterFunction((TriplePattern) tp));
				else if (tp instanceof Functor)
					ccps.addJoinComponent(new BaseAtomFilterFunction((Functor) tp));
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula){
				translateBody(f, ccps);
			}
		} else if (formula instanceof RIFOr){
			OptionalProductionSystems options = new OptionalProductionSystems();
			for (RIFFormula f : (RIFOr) formula){
				ContextCPS ruleret = new ContextCPS();
				
				translateBody(f,ruleret);
				
				options.add(ruleret);
			}
			ccps.addOption(options);
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
			RIFExternalValue val = (RIFExternalValue) formula;
			ccps.addPredicate(ExternalFunctionRegistry.compile(val));
		} else {
			throw new UnsupportedOperationException("RIF translation: Unrecognised formula expression type.");
		}
	}
	
	protected void translateHead(String ruleID, RIFFormula formula, ContextCPS ccps) throws RIFPredicateException, UnsupportedOperationException {
		if (formula instanceof RIFAtomic){
			List<ClauseEntry> triples = translate((RIFAtomic) formula, ccps);
			for (ClauseEntry tp : triples){
				if (tp instanceof TriplePattern)
					ccps.addConsequence(new RIFTripleConsequence((TriplePattern) tp, ruleID));
				else if (tp instanceof Functor)
					ccps.addConsequence(new RIFAtomConsequence((Functor) tp, ruleID));
				ccps.setReentrant(true);
			}
		} else if (formula instanceof RIFAnd){
			for (RIFFormula f : (RIFAnd) formula) {
				translateHead(ruleID, f, ccps);
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
	
	protected List<ClauseEntry> translate(RIFAtomic atomic, ContextCPS ccps) throws UnsupportedOperationException {
		List<ClauseEntry> clauses = new ArrayList<ClauseEntry>();
		if (atomic instanceof RIFAtom){
			RIFAtom atom = (RIFAtom) atomic;
			// Catch special cases, if the uri is a local uri or if the iri is a rif:error, to treat as Atoms
			if (atom.getOp().getDatatype().equals(RIFLocalConst.datatype)
					|| (atom.getOp().getDatatype().equals(RIFIRIConst.datatype)
							&& atom.getOp().getNode().getURI().toLowerCase().endsWith("error")
						)
					){
				clauses.add(translateAtom(atom, ccps));
			}else{
			// Try to treat all other cases like triples.
				Node subject, predicate, object;
				subject = translate(atom.getArg(0), ccps);
				switch (atom.getArgsSize()){
					case 1:
						object = translate(atom.getOp(), ccps);
						predicate = NodeFactory.createURI(RIFMemberFilterFunction.RDF_TYPE_URI);
						clauses.add(new TriplePattern(subject, predicate, object));
						break;
					case 2:
						object = translate(atom.getArg(1), ccps);
						predicate = translate(atom.getOp(), ccps);
						clauses.add(new TriplePattern(subject, predicate, object));
						break;
					case 3:
						if (atom.getOp().getNode().isLiteral() && atom.getOp().getNode().getLiteralValue().equals("triple")){
							object = translate(atom.getArg(2), ccps);
							predicate = translate(atom.getArg(1), ccps);
							clauses.add(new TriplePattern(subject, predicate, object));
							break;
						}
					default:
				// If the RIFAtom can't be a triple, treat it like an atom.
						clauses.add(translateAtom(atom, ccps));
				}
			}
		} else if (atomic instanceof RIFFrame) {
			RIFFrame frame = (RIFFrame) atomic;
			Node subject = translate(frame.getSubject(), ccps);
			for (int i = 0; i < frame.getPredicateObjectPairCount(); i++){
				Node predicate = translate(frame.getPredicate(i), ccps);
				Node object = translate(frame.getObject(i), ccps);
				clauses.add(new TriplePattern(subject, predicate, object));
			}
		} else throw new UnsupportedOperationException("RIF translation: Unrecognised atomic type.");
		
		return clauses;
	}
	
	protected Functor translateAtom(RIFAtom atom, ContextCPS ccps){
		String name;
		try {
			name = atom.getOp().getNode().getURI();
		} catch (UnsupportedOperationException e) {
			throw new UnsupportedOperationException("RIF translation: Atom operators must be URIs.", e);
		}
		
		Node[] args = new Node[atom.getArgsSize()];
		try{
			for (int i = 0; i < args.length; i++){
				args[i] = ((RIFDatum)atom.getArg(i)).getNode();
			}
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException("RIF translation: Currently not supporting lists in Atoms.", e);
		}
		
		return new Functor(name, args);
	}
	
	protected Node translate(RIFData data, ContextCPS ccps) throws UnsupportedOperationException {
		if (data instanceof RIFDatum){
			RIFDatum datum = (RIFDatum) data;
			Node ret = datum.getNode();
			if (datum instanceof RIFFunction){
				if (datum instanceof RIFExpr){
					ccps.addPredicate(RIF_LIB.compile((RIFExpr) datum));
				} else if (datum instanceof RIFExternalExpr){
					ccps.addPredicate(ExternalFunctionRegistry.compile((RIFExternal) datum));
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