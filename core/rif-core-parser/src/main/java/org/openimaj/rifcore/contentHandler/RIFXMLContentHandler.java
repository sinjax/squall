package org.openimaj.rifcore.contentHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.openimaj.rifcore.RIFMetaHolder;
import org.openimaj.rifcore.RIFRuleSet;
import org.openimaj.rifcore.conditions.RIFExternal;
import org.openimaj.rifcore.conditions.atomic.RIFAtom;
import org.openimaj.rifcore.conditions.atomic.RIFFrame;
import org.openimaj.rifcore.conditions.data.RIFConst;
import org.openimaj.rifcore.conditions.data.RIFList;
import org.openimaj.rifcore.conditions.data.RIFVar;
import org.openimaj.rifcore.conditions.formula.RIFEqual;
import org.openimaj.rifcore.conditions.formula.RIFFormula;
import org.openimaj.rifcore.conditions.formula.RIFMember;
import org.openimaj.rifcore.rules.RIFForAll;
import org.openimaj.rifcore.rules.RIFGroup;
import org.openimaj.rifcore.rules.RIFRule;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public abstract class RIFXMLContentHandler extends DefaultHandler {

	protected enum Element {
		//IRIMETA
		ID("id")
		,
			IRICONST("Const")
			,
		META("meta")
		,
//			GENERIC_FORMULA
//			,
		//DOCUMENT
		DOCUMENT("Document")
		,
			DIRECTIVE("directive")
			,
				IMPORT("Import")
				,
					LOCATION("location")
					,
					PROFILE("profile")
			,
			PAYLOAD("payload")
			,
				GROUP("Group")	// <- Generic GROUP
				,
					SENTENCE("sentence")
					,
//						Generic GROUP
//						,
						FOR_ALL("Forall")
						,
							DECLARE("declare")
							,
								VAR("Var")	// <- Generic VAR
								,
							FOR_ALL_FORMULA("formula")
							,
								IMPLIES("Implies")// <- Generic IMPLIES
								,
									IF("if")
									,
//									GENERIC_FORMULA:
										AND("And")
										,
											FORMULA("formula")	// <- Generic FORMULA
											,
//												GENERIC_FORMULA
//												,
										OR("Or")
										,
//											Generic FORMULA
//											,
										EXISTS("Exists")
										,
//											Generic DECLARE
//											,
//											Generic FORMULA
//											,
										EQUAL("Equal")
										,
											LEFT("left")
											,
												CONST("Const")	// <- Generic CONST
												,
//												Generic VAR
//												,
												LIST("List")	// <- Generic LIST
												,
													ITEMS("items")
													,
//														Generic CONST
//														,
//														Generic LIST
//														,
//														Generic EXTERNAL_EXPR
												EXTERNAL_EXPR("External")	// <- Generic EXTERNAL_EXPR
												,
													EXTERNAL_EXPR_CONTENT("content")
													,
														EXPR("Expr")
														,
//															Generic OP
//															,
//															Generic ARGS
//															,
											RIGHT("right")
											,
//												Generic CONST
//												,
//												Generic VAR
//												,
//												Generic LIST
//												,
//												Generic EXTERNAL_EXPR
//												,
										MEMBER("Member")
										,
											INSTANCE("instance")
											,
//												Generic CONST
//												,
//												Generic VAR
//												,
//												Generic LIST
//												,
//												Generic EXTERNAL_EXP
//												,
											CLASS("class")
											,
//												Generic CONST
//												,
//												Generic VAR
//												,
//												Generic LIST
//												,
//												Generic EXTERNAL_EXPR
//												,
										FORMULA_EXTERNAL("External")
										,
											FORMULA_CONTENT("content")
											,
//												Generic ATOM
//												,
										FRAME("Frame")	// <- Generic FRAME
										,
											OBJECT("object")
											,
//												Generic CONST
//												,
//												Generic VAR
//												,
//												Generic LIST
//												,
//												Generic EXTERNAL_EXPR
//												,
											SLOT("slot")
											,
												SLOT_FIRST("<imaginary>")
												,
//												2x{
//													Generic CONST
//													,
//													Generic VAR
//													,
//													Generic LIST
//													,
//													Generic EXTERNAL_EXPR
//													,
//												}x2
										ATOM("Atom")	// <- Generic Atom
										,
											OP("op")	// <- Generic OP
											,
//												Generic CONST
//														,
											ARGS("args")	// <- Generic ARGS
											,
												NAME("Name")	// <- Generic NAME
												,
//												Generic CONST
//												,
//												Generic VAR
//												,
//												Generic LIST
//												,
//												Generic EXTERNAL_EXPR
//												,
									THEN("then")
									,
//										GENERIC_FORMULA
//										,
//								Generic FRAME
//								,
//								Generic ATOM
//								,
//						,
//						Generic IMPLIES
//						,
											
		// START RIF-Core
//			Generic FRAME
//			,
			META_AND("And")
			,
				META_FORMULA("formula")
				,
//					Generic FRAME
//					,
									
			GROUND_EXTERNAL_EXPR("External")	// <- Generic GROUND_EXTERNAL_EXPR
			,									//    Replaces Generic EXTERNAL_EXPR in LIST
				GROUND_EXTERNAL_EXPR_CONTENT("content")
				,
					GROUND_EXPR("Expr")	// <- Generic GROUND_EXPR
					,
//						Generic OP
//						,
						GROUND_ARGS("args")	// <- Generic GROUND_ARGS
						,
//							Generic NAME
//							,
//							Generic CONST
//							,
//							Generic LIST
//							,
//							Generic GROUND_EXTERNAL
//							,
						
			GROUND_ATOM("Atom")	// Generic GROUND_ATOM
			,					// Is not used explicitly, but is stated to mimic the behaviour of EXPR elements during parsing. 
//				Generic OP
//				,
//				Generic GROUND_ARGS
//				,
						
			THEN_AND("And")	// Co-replaces GENERIC_FORMULA  
			,
				THEN_AND_FORMULA("formula")
//				,
//					Generic ATOM
//					,
//					Generic FRAME
//					,
//			Generic ATOM	// Co-replaces GENERIC_FORMULA
//			,
//			Generic FRAME	// Co-replaces GENERIC_FORMULA
//			,
		// END RIF-Core
		;
		
		private final String tag;
		
		private Element(String tag){
			this.tag = tag;
		}
		
		public String toString(){
			return tag;
		}
	}
	
	protected RIFRuleSet ruleSet;

	/**
	 * 
	 */
	public RIFXMLContentHandler(){

	}
	
	/**
	 * @param rs
	 */
	public void setRuleSet (RIFRuleSet rs){
		if (this.ruleSet == null)
			this.ruleSet = rs;
	}
	
	/**
	 * 
	 * @return -
	 * 		The {@link RIFRuleSet} originally handed to the ContentHandler.
	 */
	public RIFRuleSet getRuleSet() {
		return this.ruleSet;
	}

	protected Stack<Element> descent;
	protected Stack<Element> lastSibling;
	
	protected StringBuilder partialContent;
	
	protected URI currentLocation;
	protected URI currentProfile;
	
	protected RIFMetaHolder currentMetaHolder;

	protected Stack<RIFGroup> currentGroup;
	
	protected RIFForAll currentForAll;
	protected RIFRule currentRule;
	protected LinkedList<RIFFormula> currentFormula;
	protected RIFEqual currentEqual;
	protected RIFMember currentMember;
	
	protected Stack<RIFExternal> currentExternal;
	protected RIFAtom currentAtom;
	protected RIFFrame currentFrame;
	
	protected Stack<RIFList> currentList;
	protected RIFVar currentVar;
	protected RIFConst<?> currentConst;
	
	protected Stack<List<String>> names;
	
	//   !ENTITY HANDLING
	
	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		if (ruleSet == null)
			throw new NullPointerException("RIFRuleSet passed to RIFXMLContentHandler cannot be null.");
		
		if (prefix != null && !prefix.equals(""))
			ruleSet.addPrefix(prefix, findURI(uri));
	}
	
	//   DOCUMENT HANDLING
	
	@Override
	public void startDocument() throws SAXException {
		if (ruleSet == null)
			throw new NullPointerException("RIFRuleSet passed to RIFXMLContentHandler cannot be null.");
		
		descent = new Stack<Element>();
		lastSibling = new Stack<Element>();
		
		partialContent = null;
		
		currentLocation = null;
		currentProfile = null;
		
		currentMetaHolder = null;
		
		currentGroup = null;
		currentFormula = new LinkedList<RIFFormula>();
		currentExternal = new Stack<RIFExternal>();
		currentList = new Stack<RIFList>();
		
		names = new Stack<List<String>>();
		names.push(new ArrayList<String>());
	}
	
	@Override
	public void endDocument() throws SAXException {
		
	}
	
	protected URI findURI(String value) throws SAXException{
		try {
			return new URI(value);
		} catch (URISyntaxException e) {
//System.out.println("Looking up Prefix");
			if (value.charAt(0) == '&'){
				int endOfEntity = value.indexOf(';');
				String prefix = value.substring(1, endOfEntity);
				String uri = ruleSet.getPrefix(prefix).toString();
				if (endOfEntity < value.length()){
					uri += value.substring(endOfEntity + 1);
				}
				try {
					return new URI(uri);
				} catch (URISyntaxException ex) {
					throw new SAXException("RIF-Core: IRI <"+uri+"> does not follow URI syntax.",e);
				}
			}else{
				throw new SAXException("RIF-Core: IRI <"+value+"> does not follow URI syntax.",e);
			}
		}
	}

}