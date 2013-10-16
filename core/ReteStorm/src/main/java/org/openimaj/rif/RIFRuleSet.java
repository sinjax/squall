package org.openimaj.rif;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.*;

import org.openimaj.rif.conditions.atomic.RIFAtom;
import org.openimaj.rif.conditions.atomic.RIFFrame;
import org.openimaj.rif.conditions.data.RIFData;
import org.openimaj.rif.conditions.data.RIFList;
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
import org.openimaj.rif.rules.RIFForAll;
import org.openimaj.rif.rules.RIFGroup;
import org.openimaj.rif.rules.RIFRule;
import org.openimaj.rif.rules.RIFSentence;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.*;

/**
 * Converts a set of rules expressed in RIF/XML to a set of rules in a different format.
 * @author David Monks <david.monks@zepler.net>
 */
public class RIFRuleSet implements Iterable<RIFSentence> {

	/**
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println("%s %s".replaceFirst("%s", "Hello"));
		try {
			RIFRuleSet rs = RIFRuleSet.parse(new URI("http://www.w3.org/2005/rules/test/repository/tc/Frames/Frames-premise.rif"));
			System.out.println(rs.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the RIF Rule Set from the XML document at the rulesURI.
	 * @param rulesURI -
	 * 			The URI of the rule set whose rules are to be converted.
	 * @param conH -
	 * 			The {@link RIFXMLContentHandler} with which the XML at the rulesURI should be parsed into a {@link RIFRuleSet}.
	 * @return 
	 * 			True if the rule set was successfully converted, false otherwise.
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static RIFRuleSet parse(URI rulesURI, RIFXMLContentHandler conH) throws IOException, SAXException {
		//Fetch the rules from the rulesURI and load into a SAX parser.
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    try {
			spf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			spf.setFeature("http://xml.org/sax/features/namespaces", true);
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    SAXParser saxParser;
	    RIFRuleSet ruleSet = conH.getRuleSet();
	    
		try {
			saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
		    xmlReader.setContentHandler(conH);
		    xmlReader.parse(new InputSource(rulesURI.toASCIIString()));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ruleSet;
	}
	
	//  Static Values
	
	public static final RIFCoreXMLContentHandler RIF_CORE = new RIFCoreXMLContentHandler();
	
	//  VARIABLES
	
	private URI base;
	private Map<String,URI> prefixes;
	private Map<URI,URI> imports;
	private Set<RIFSentence> groups;
	
	//  CONSTRUCTORS
	
	/**
	 * 
	 */
	public RIFRuleSet(){
		this.prefixes = new HashMap<String, URI>();
		this.imports = new HashMap<URI, URI>();
		this.groups = new HashSet<RIFSentence>();
	}
	
	/**
	 * @param base
	 * @param prefixes
	 */
	public RIFRuleSet(URI base,
					  Map<String,URI> prefixes){
		super();
		
		this.base = base;
		for (String pref : prefixes.keySet())
			this.prefixes.put(pref, prefixes.get(pref));
	}
	
	/**
	 * @param imports
	 */
	public RIFRuleSet(Map<URI,URI> imports){
		super();
	}
	
	/**
	 * @param base
	 * @param prefixes
	 * @param imports
	 */
	public RIFRuleSet(URI base,
					  Map<String,URI> prefixes,
					  Map<URI,URI> imports){
		super();
		
		this.base = base;
		for (String pref : prefixes.keySet())
			this.prefixes.put(pref, prefixes.get(pref));
		
		for (URI loc : imports.keySet())
			this.imports.put(loc, imports.get(loc));
	}
	
	/**
	 * @param base
	 * @param prefixes
	 * @param imports
	 * @param rules
	 */
	public RIFRuleSet(URI base,
					  Map<String,URI> prefixes,
					  Map<URI,URI> imports,
					  Set<RIFSentence> rules){
		super();
		
		this.base = base;
		for (String pref : prefixes.keySet())
			this.prefixes.put(pref, prefixes.get(pref));
		
		for (URI loc : imports.keySet())
			this.imports.put(loc, imports.get(loc));
		
		for (RIFSentence r : rules)
			this.groups.add(r);
	}
	
	//  GETTERS AND SETTERS
	
	/**
	 * @param base
	 */
	public void setBase(URI base){
		this.base = base;
	}
	
	/**
	 * @return
	 */
	public URI getBase(){
		return this.base;
	}
	
	/**
	 * @param pref
	 * @param loc
	 */
	public void addPrefix(String pref, URI loc){
		this.prefixes.put(pref, loc);
	}
	
	/**
	 * @param pref
	 * @return
	 */
	public URI getPrefix(String pref){
		return this.prefixes.get(pref);
	}
	
	/**
	 * @return
	 */
	public Set<String> getPrefixKeySet(){
		return this.prefixes.keySet();
	}
	
	/**
	 * @param loc
	 * @param prof
	 */
	public void addImport(URI loc, URI prof){
		this.imports.put(loc, prof);
	}
	
	/**
	 * @param loc
	 * @return
	 */
	public URI getImport(URI loc){
		return this.imports.get(loc);
	}
	
	/**
	 * @return
	 */
	public Set<URI> getImportKeySet(){
		return this.imports.keySet();
	}
	
	/**
	 * @param rule
	 */
	public void addRule(RIFSentence rule){
		this.groups.add(rule);
	}

	@Override
	public Iterator<RIFSentence> iterator(){
		return this.groups.iterator();
	}
	
	public String toString(){
		String fbase = "base: "+ (base == null ? "" : base.toString());
		String fprefixes = "prefixes:";
		for (String pref : prefixes.keySet()){
			fprefixes += "\n\t[ "+pref+": "+prefixes.get(pref).toString()+" ]";
		}
		String fimports = "imports:";
		for (URI loc : imports.keySet()){
			fimports += "\n\t[ loc: "+loc.toString()+", prof: "+(imports.get(loc) == null ? "" : imports.get(loc).toString())+" ]";
		}
		String fgroups = "groups:";
		for (RIFSentence sentence : groups){
			fgroups += "\n"+sentence.toString();
		}
		return "[ \n  "+fbase+"\n  "+fprefixes+"\n  "+fimports+"\n  "+fgroups+"\n]";
	}
	
	//  RIFXMLContentHandler Class
	
	public static abstract class RIFXMLContentHandler extends DefaultHandler {
	
		protected enum Element {
			//IRIMETA
			ID("id")
			,
				IRICONST("Const")
				,
			META("meta")
			,
//				Generic FRAME
//				,
				META_AND("And")
				,
					META_FORMULA("formula")
					,
//						Generic FRAME
//						,
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
//							Generic GROUP
//							,
							FOR_ALL("Forall")
							,
								DECLARE("declare")
								,
									VAR("Var")	// <- Generic VAR
									,
										NAME("Name")
										,
								FOR_ALL_FORMULA("formula")
								,
									IMPLIES("Implies")// <- Generic IMPLIES
									,
										IF("if")
										,
//										GENERIC_FORMULA:
											AND("And")
											,
												FORMULA("formula")
												,
//													GENERIC_FORMULA
//													,
											OR("Or")
											,
//												FORMULA	//formula
//												,
//													GENERIC_FORMULA
//													,
											EXISTS("Exists")
											,
//												DECLARE	//declare
//												,
//													Generic VAR
//													,
//												FORMULA	//formula
//												,
//													GENERIC_FORMULA
//													,
											EQUAL("Equal")
											,
												LEFT("left")
												,
													CONST("Const")	// <- Generic CONST
													,
//													Generic VAR
//													,
													LIST("List")	// <- Generic LIST
													,
														ITEMS("items")
														,
//															Generic CONST
//															,
//															Generic LIST
//															,
															GROUND_EXTERNAL("External")	// <- Generic GROUND_EXTERNAL
															,
																GROUND_CONTENT("content")
																,
																	GROUND_EXPR("Expr")
																	,
//																		OP	//op
//																		,
//																			Generic CONST
//																			,
																		GROUND_ARGS("args")
																		,
//																			Generic CONST
//																			,
//																			Generic LIST
//																			,
//																			Generic GROUND_EXTERNAL
//																			,
																		
													TERM_EXTERNAL("External")	// <- Generic TERM_EXTERNAL
													,
														TERM_CONTENT("content")
														,
															TERM_EXPR("Expr")
															,
//																OP	//op
//																,
//																	Generic CONST
//																	,
//																Generic ARGS
//																,
												RIGHT("right")
												,
//													Generic CONST
//													,
//													Generic VAR
//													,
//													Generic LIST
//													,
//													Generic TERM_EXTERNAL
//													,
											MEMBER("Member")
											,
												INSTANCE("instance")
												,
//													Generic CONST
//													,
//													Generic VAR
//													,
//													Generic LIST
//													,
//													Generic TERM_EXTERNAL
//													,
												CLASS("class")
												,
//													Generic CONST
//													,
//													Generic VAR
//													,
//													Generic LIST
//													,
//													Generic TERM_EXTERNAL
//													,
											FORMULA_EXTERNAL("External")
											,
												FORMULA_CONTENT("content")
												,
													ATOM("Atom")
													,
														OP("op")
														,
//															Generic CONST
//															,
														TERM_ARGS("args")	// <- Generic ARGS
														,
//															Generic CONST
//															,
//															Generic VAR
//															,
//															Generic LIST
//															,
//															Generic TERM_EXTERNAL
//															,
//											Generic FRAME
//											,
//											Generic ATOM
//											,
										THEN("then")
										,
//											ATOM	//Atom
//											,
//												OP	//op
//												,
//													Generic CONST
//													,
//												Generic ARGS
//												,
											FRAME("Frame")	// <- Generic FRAME
											,
												OBJECT("object")
												,
//													Generic CONST
//													,
//													Generic VAR
//													,
//													Generic LIST
//													,
//													Generic TERM_EXTERNAL
//													,
												SLOT("slot")
												,
													SLOT_FIRST("<imaginary>")
													,
//													2x{
//														Generic CONST
//														,
//														Generic VAR
//														,
//														Generic LIST
//														,
//														Generic TERM_EXTERNAL
//														,
//													}x2
											THEN_AND("And")
											,
												THEN_AND_FORMULA("formula")
//												,
//													ATOM	//Atom
//													,
//														OP	//op
//														,
//															Generic CONST
//															,
//														Generic ARGS
//														,
//													Generic FRAME
//													,
//									Generic FRAME
//									,
//									Generic ATOM
//									,
//							,
//							Generic IMPLIES
//							,
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
		
		protected RIFXMLContentHandler (){
			this.ruleSet = new RIFRuleSet();
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

		protected Stack<RIFGroup> currentGroup;
		
		protected RIFForAll currentForAll;
		protected RIFRule currentRule;
		protected Stack<RIFFormula> currentFormula;
		protected RIFEqual currentEqual;
		protected RIFMember currentMember;
		
		protected Stack<RIFExternal> currentExternal;
		protected RIFAtom currentAtom;
		protected RIFFrame currentFrame;
		
		protected Stack<RIFList> currentList;
		protected RIFVar currentVar;
		protected RIFConst<?> currentConst;
		
		//   !ENTITY HANDLING
		
		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			if (prefix != null && !prefix.equals(""))
				ruleSet.addPrefix(prefix, findURI(uri));
		}
		
		//   DOCUMENT HANDLING
		
		@Override
		public void startDocument() throws SAXException {
			descent = new Stack<Element>();
			lastSibling = new Stack<Element>();
			
			partialContent = null;
			
			currentLocation = null;
			currentProfile = null;
			
			currentGroup = null;
			currentFormula = new Stack<RIFFormula>();
			currentExternal = new Stack<RIFExternal>();
			currentList = new Stack<RIFList>();
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
						throw new SAXException("RIF: IRI <"+uri+"> does not follow URI syntax.",e);
					}
				}else{
					throw new SAXException("RIF: IRI <"+value+"> does not follow URI syntax.",e);
				}
			}
		}
	
	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 *
	 */
	public static class RIFCoreXMLContentHandler extends RIFXMLContentHandler {
		
		protected RIFCoreXMLContentHandler(){
			super();
		}
		
		@Override
		public void startElement(		String namespaceURI,
	            				 		String localName,
	            				 String qName, 
	            				 		Attributes atts)
	           throws SAXException {
			handleContent();
			
			if (localName == null || localName.equals(""))
				localName = qName;
			
//System.out.println(localName);
			
			RIFData d = null;
			
			if (descent.isEmpty()){
				if (localName.equals(Element.DOCUMENT.toString())){
					descent.push(Element.DOCUMENT);
					lastSibling.push(null);
					
					if (atts.getValue("xmlns") != null){
						ruleSet.setBase(findURI(atts.getValue("xmlns")));
					}
				}
			}else{
				switch (descent.peek()){
					case ID:
						switch (localName.charAt(0)){
							case 'C':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Const' element, if it exists, must be the first child of an 'id' element.");
								descent.push(Element.IRICONST);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'Const' expected, '"+localName+"' found.");
						}
						break;
					case META:
						switch (localName.charAt(0)){
							case 'F':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' elements must be the sole child of a 'meta' element.");
								descent.push(Element.FRAME);
								lastSibling.push(null);
								
								currentFrame = new RIFFrame();
								
								break;
							case 'A':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'And' elements must be the sole child of a 'meta' element.");
								descent.push(Element.META_AND);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'And' or 'Frame' expected, '"+localName+"' found.");
						}
						break;
					case META_AND:
						switch (localName.charAt(0)){
							case 'f':
								descent.push(Element.META_FORMULA);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'formula' expected, '"+localName+"' found.");
						}
						break;
					case META_FORMULA:
						switch (localName.charAt(0)){
							case 'F':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' elements must be the sole child of a 'formula' element when within a 'meta'.");
								descent.push(Element.FRAME);
								lastSibling.push(null);
								
								currentFrame = new RIFFrame();
								
								break;
							default:
								throw new SAXException("RIF: 'Frame' expected, '"+localName+"' found.");
						}
						break;
						
					case DOCUMENT:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'd':
								if (lastSibling.peek() == Element.PAYLOAD) throw new SAXException("RIF: All 'directive' elements must preceed any 'payload' element.");
								descent.push(Element.DIRECTIVE);
								lastSibling.push(null);
								break;
							case 'p':
								if (lastSibling.peek() == Element.PAYLOAD) throw new SAXException("RIF: 'payload' elements must be unique within a 'directive' element.");
								descent.push(Element.PAYLOAD);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'directive' or 'payload' expected, '"+localName+"' found.");
						}
						break;
					case DIRECTIVE:
						switch (localName.charAt(0)){
							case 'I':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Import' elements must be the sole child of a 'directive' element.");
								descent.push(Element.IMPORT);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'Import' expected, '"+localName+"' found.");
						}
						break;
					case IMPORT:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'l':
								if (lastSibling.peek() == Element.LOCATION) throw new SAXException("RIF: 'location' element must be unique within an 'Import' element.");
								if (lastSibling.peek() == Element.PROFILE) throw new SAXException("RIF: 'location' element must preceed any 'profile' element within an 'Import' element.");
								descent.push(Element.LOCATION);
								lastSibling.push(null);
								break;
							case 'p':
								if (lastSibling.peek() != Element.LOCATION) throw new SAXException("RIF: 'profile' element, if it exists, must be preceeded by a 'location' element.");
								descent.push(Element.PROFILE);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'location' or 'profile' expected, '"+localName+"' found.");
						}
						break;
						
					case PAYLOAD:
						switch (localName.charAt(0)){
							case 'G':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Group' element must be within a 'payload' element.");
								descent.push(Element.GROUP);
								lastSibling.push(null);
								
								if (currentGroup == null)
									currentGroup = new Stack<RIFGroup>();
								else
									throw new SAXException("RIF: Should not have constructed the Group stack before first Group in payload.");
								
								break;
							default:
								throw new SAXException("RIF: 'Group' expected, '"+localName+"' found.");
						}
						break;
					case GROUP:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 's':
								descent.push(Element.SENTENCE);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta' or 'sentence' expected, '"+localName+"' found.");
						}
						break;
					case SENTENCE:
						switch (localName.charAt(0)){
							case 'G':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Group' element must be the sole child of a 'sentence' element.");
								descent.push(Element.GROUP);
								lastSibling.push(null);
								
								RIFGroup g = new RIFGroup();
								if (currentGroup.isEmpty())
									this.ruleSet.groups.add(g);
								else
									currentGroup.peek().addSentence(g);
								currentGroup.push(g);
								
								break;
							case 'F':
								switch(localName.charAt(1)){
									case 'o':
										if (lastSibling.peek() != null) throw new SAXException("RIF: 'Forall' element must be the sole child of a 'sentence' element.");
										descent.push(Element.FOR_ALL);
										lastSibling.push(null);
										
										currentForAll = new RIFForAll();
										if (currentGroup.isEmpty())
											this.ruleSet.groups.add(currentForAll);
										else
											currentGroup.peek().addSentence(currentForAll);
										
										break;
									case 'r':
										if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
										descent.push(Element.FRAME);
										lastSibling.push(null);
										
										currentFrame = new RIFFrame();
										if (currentGroup.isEmpty())
											this.ruleSet.groups.add(currentFrame);
										else
											currentGroup.peek().addSentence(currentFrame);
										
										break;
									default:
										throw new SAXException("RIF: 'Group', 'ForAll', 'Frame', 'Atom' or 'Implies' expected, '"+localName+"' found.");
								}
								
								break;
							case 'I':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Implies' element must be the sole child of a 'sentence' element.");
								descent.push(Element.IMPLIES);
								lastSibling.push(null);
								
								currentRule = new RIFRule();
								if (currentGroup.isEmpty())
									this.ruleSet.groups.add(currentRule);
								else
									currentGroup.peek().addSentence(currentRule);
								
								break;
							case 'A':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
								descent.push(Element.ATOM);
								lastSibling.push(null);
								
								currentAtom = new RIFAtom();
								if (currentGroup.isEmpty())
									this.ruleSet.groups.add(currentAtom);
								else
									currentGroup.peek().addSentence(currentAtom);
								
								break;
							default:
								throw new SAXException("RIF: 'Group', 'ForAll', 'Frame', 'Atom' or 'Implies' expected, '"+localName+"' found.");
						}
						break;
					case FOR_ALL:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'd':
								if (lastSibling.peek() == Element.FOR_ALL_FORMULA) throw new SAXException("RIF: 'declare' element must be stated before any 'formula' element when within a 'Forall' element.");
								descent.push(Element.DECLARE);
								lastSibling.push(null);
								break;
							case 'f':
								if (lastSibling.peek() != Element.DECLARE) throw new SAXException("RIF: 'formula' element must be preceeded by at least one 'declare' element when within a 'Forall' element.");
								descent.push(Element.FOR_ALL_FORMULA);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'declare' or 'formula' expected, '"+localName+"' found.");
						}
						break;
					case DECLARE:
						switch (localName.charAt(0)){
							case 'V':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Var' elements must be the sole child of a 'declare' element.");
								descent.push(Element.VAR);
								lastSibling.push(null);
								
								currentVar = new RIFVar();
								if (currentFormula.isEmpty())
									currentForAll.addUniversalVar(currentVar);
								else
									try {
										((RIFExists) currentFormula.peek()).addExistentialVar(currentVar);
									} catch (ClassCastException e) {
										throw new SAXException("RIF: variable declarations can only occur as direct children of 'ForAll' or 'Exists' elements", e);
									}
								
								break;
							default:
								throw new SAXException("RIF: 'Var' expected, '"+localName+"' found.");
						}
						break;
					case VAR:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'N':
								if (lastSibling.peek() == Element.NAME) throw new SAXException("RIF: 'Name' element must be unique within a 'Var' element.");
								descent.push(Element.NAME);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta' or 'Name' expected, '"+localName+"' found.");
						}
						break;
					case FOR_ALL_FORMULA:
						switch (localName.charAt(0)){
							case 'I':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Implies' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
								descent.push(Element.IMPLIES);
								lastSibling.push(null);
								
								currentRule = new RIFRule();
								currentForAll.setStatement(currentRule);
								
								break;
							case 'A':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
								descent.push(Element.ATOM);
								lastSibling.push(null);
								
								currentAtom = new RIFAtom();
								currentForAll.setStatement(currentAtom);
								
								break;
							case 'F':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
								descent.push(Element.FRAME);
								lastSibling.push(null);
								
								currentFrame = new RIFFrame();
								currentForAll.setStatement(currentFrame);
								
								break;
							default:
								throw new SAXException("RIF: 'Implies', 'Atom' or 'Frame' expected, '"+localName+"' found.");
						}
						break;
					case IMPLIES:
						switch (localName.charAt(0)){
							case 'i':
								switch (localName.charAt(1)){
									case 'd':
										if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
										descent.push(Element.ID);
										lastSibling.push(null);
										break;
									case 'f':
										if (lastSibling.peek() == Element.IF) throw new SAXException("RIF: 'if' element must be unique within an 'Implies' element.");
										if (lastSibling.peek() == Element.THEN) throw new SAXException("RIF: 'if' element must preceed any 'then' element within an 'Implies' element.");
										descent.push(Element.IF);
										lastSibling.push(null);
										break;
									default:
										throw new SAXException("RIF: 'id', 'meta', 'if' or 'then' expected, '"+localName+"' found.");
								}
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 't':
								if (lastSibling.peek() != Element.IF) throw new SAXException("RIF: 'then' element must follow an 'if' element within an 'Implies' element.");
								descent.push(Element.THEN);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'if' or 'then' expected, '"+localName+"' found.");
						}
						break;
					
					case IF:
					case FORMULA:
						startFORMULA(localName);
						break;
					case EXISTS:
						switch (localName.charAt(0)){
							case 'd':
								if (lastSibling.peek() == Element.FORMULA) throw new SAXException("RIF: 'declare' element must preceed the 'formula' element when within an 'Exists' element.");
								descent.push(Element.DECLARE);
								lastSibling.push(null);
								break;
							case 'f':
								if (lastSibling.peek() != Element.DECLARE) throw new SAXException("RIF: 'formula' element, if it exists, must be preceeded by a 'declare' element when within an 'Exists' element.");
								descent.push(Element.FORMULA);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'declare' or 'formula' expected, '"+localName+"' found.");
						}
					case AND:
					case OR:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'f':
								descent.push(Element.FORMULA);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta' or 'formula' expected, '"+localName+"' found.");
						}
						break;
					case FRAME:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'o':
								if (lastSibling.peek() == Element.OBJECT) throw new SAXException("RIF: 'object' element must be unique within a 'Frame' element.");
								if (lastSibling.peek() == Element.SLOT) throw new SAXException("RIF: 'object' element must preceed any 'slot' element within a 'Frame' element.");
								descent.push(Element.OBJECT);
								lastSibling.push(null);
								break;
							case 's':
								if (lastSibling.peek() != Element.OBJECT) currentFrame.newPredObPair();
								descent.push(Element.SLOT);
								descent.push(Element.SLOT_FIRST);
								lastSibling.push(null);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'object' or 'slot' expected, '"+localName+"' found.");
						}
						break;
					case EQUAL:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'l':
								if (lastSibling.peek() == Element.LEFT) throw new SAXException("RIF: 'left' element must be unique within an 'Equal' element.");
								if (lastSibling.peek() == Element.RIGHT) throw new SAXException("RIF: 'left' element must preceed any 'right' element within an 'Equal' element.");
								descent.push(Element.LEFT);
								lastSibling.push(null);
								break;
							case 'r':
								if (lastSibling.peek() != Element.LEFT) throw new SAXException("RIF: 'right' element must be preceeded by a 'left' element within an 'Equal' element.");
								descent.push(Element.RIGHT);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'left' or 'right' expected, '"+localName+"' found.");
						}
						break;
					case MEMBER:
						switch (localName.charAt(0)){
							case 'i':
								switch (localName.charAt(1)){
									case 'd':
										if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
										descent.push(Element.ID);
										lastSibling.push(null);
										break;
									case 'n':
										if (lastSibling.peek() == Element.INSTANCE) throw new SAXException("RIF: 'instance' element must be unique within an 'Memeber' element.");
										if (lastSibling.peek() == Element.CLASS) throw new SAXException("RIF: 'instance' element must preceed any 'class' element within an 'Member' element.");
										descent.push(Element.INSTANCE);
										lastSibling.push(null);
										break;
									default:
										throw new SAXException("RIF: 'id', 'meta', 'instance' or 'class' expected, '"+localName+"' found.");
								}
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'c':
								if (lastSibling.peek() != Element.INSTANCE) throw new SAXException("RIF: 'class' element must be preceeded by an 'instance' element within an 'Equal' element.");
								descent.push(Element.CLASS);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'instance' or 'class' expected, '"+localName+"' found.");
						}
						break;
					case SLOT_FIRST:
						if (lastSibling.peek() == null){
							d = startTERM(localName,atts);
							if (d instanceof RIFDatum)
								currentFrame.setPredicate((RIFDatum) d);
							else
								throw new SAXException("RIF: 'slot' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
						}else{
							descent.pop();
							lastSibling.pop();
							d = startTERM(localName,atts);
							if (d instanceof RIFDatum)
								currentFrame.setObject((RIFDatum) d);
							else
								throw new SAXException("RIF: 'slot' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
						}
					case OBJECT:
						if (d == null) {
							d = startTERM(localName,atts);
							if (d instanceof RIFDatum)
								currentFrame.setSubject((RIFDatum) d);
							else
								throw new SAXException("RIF: 'object' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
						}
					case RIGHT:
						if (d == null) {
							d = startTERM(localName,atts);
							if (d instanceof RIFDatum)
								currentEqual.setRight((RIFDatum) d);
							else
								throw new SAXException("RIF: 'object' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
						}
					case LEFT:
						if (d == null) {
							d = startTERM(localName,atts);
							if (d instanceof RIFDatum)
								currentEqual.setLeft((RIFDatum) d);
							else
								throw new SAXException("RIF: 'object' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
						}
					case INSTANCE:
						if (d == null) {
							d = startTERM(localName,atts);
							if (d instanceof RIFDatum)
								currentMember.setInstance((RIFDatum) d);
							else
								throw new SAXException("RIF: 'instance' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
						}
					case CLASS:
						if (d == null) {
							d = startTERM(localName,atts);
							if (d instanceof RIFDatum)
								currentMember.setInClass((RIFDatum) d);
							else
								throw new SAXException("RIF: 'class' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
						}
						if (d instanceof RIFList){
							currentList.push((RIFList) d);
						}
						break;
					case LIST:
						switch (localName.charAt(0)){
							case 'i':
								switch (localName.charAt(1)){
									case 'd':
										if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
										descent.push(Element.ID);
										lastSibling.push(null);
										break;
									case 't':
										if (lastSibling.peek() == Element.ITEMS) throw new SAXException("RIF: 'items' element, if it exists, must be unique in a 'List' element.");
										descent.push(Element.ITEMS);
										lastSibling.push(null);
									default:
										throw new SAXException("RIF: 'id', 'meta' or 'items' expected, '"+localName+"' found.");
								}
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta' or 'items' expected, '"+localName+"' found.");
						}
						break;
					case ITEMS:
						lastSibling.pop();
						lastSibling.push(null);
						d = startGROUNDTERM(localName,atts);
						currentList.peek().add(d);
						if (d instanceof RIFList){
							currentList.push((RIFList) d);
						}
						break;
					case GROUND_EXTERNAL:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'c':
								if (lastSibling.peek() == Element.GROUND_CONTENT) throw new SAXException("RIF: 'content' element must be the unique in an 'External' element.");
								descent.push(Element.GROUND_CONTENT);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta' or 'content' expected, '"+localName+"' found.");
						}
						break;
					case GROUND_CONTENT:
						switch (localName.charAt(0)){
							case 'E':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Expr' element must be the sole child of a 'content' element.");
								descent.push(Element.GROUND_EXPR);
								lastSibling.push(null);
								
								currentAtom = new RIFAtom();
								currentExternal.peek().setCommand(currentAtom);
								
								break;
							default:
								throw new SAXException("RIF: 'Expr' expected, '"+localName+"' found.");
						}
						break;
					case TERM_EXTERNAL:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'c':
								if (lastSibling.peek() == Element.TERM_CONTENT) throw new SAXException("RIF: 'content' element must be the unique in an 'External' element.");
								descent.push(Element.TERM_CONTENT);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta' or 'content' expected, '"+localName+"' found.");
						}
						break;
					case TERM_CONTENT:
						switch (localName.charAt(0)){
							case 'E':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Expr' element must be the sole child of a 'content' element.");
								descent.push(Element.TERM_EXPR);
								lastSibling.push(null);
								
								currentAtom = new RIFAtom();
								currentExternal.peek().setCommand(currentAtom);
								
								break;
							default:
								throw new SAXException("RIF: 'Expr' expected, '"+localName+"' found.");
						}
						break;
					case GROUND_EXPR:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'o':
								if (lastSibling.peek() == Element.OP) throw new SAXException("RIF: 'op' element must be unique within an 'Expr' element.");
								if (lastSibling.peek() == Element.GROUND_ARGS) throw new SAXException("RIF: 'op' element must preceed any 'args' element within an 'Expr' element.");
								descent.push(Element.OP);
								lastSibling.push(null);
								break;
							case 'a':
								if (lastSibling.peek() != Element.OP) throw new SAXException("RIF: 'args' element must be preceeded by an 'op' element within an 'Expr' element.");
								descent.push(Element.GROUND_ARGS);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'op' or 'args' expected, '"+localName+"' found.");
						}
						break;
					case TERM_EXPR:
					case ATOM:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'o':
								if (lastSibling.peek() == Element.OP) throw new SAXException("RIF: 'op' element must be unique within an 'Expr' or 'Atom' element.");
								if (lastSibling.peek() == Element.TERM_ARGS) throw new SAXException("RIF: 'op' element must preceed any 'args' element within an 'Expr' or 'Atom' element.");
								descent.push(Element.OP);
								lastSibling.push(null);
								break;
							case 'a':
								if (lastSibling.peek() != Element.OP) throw new SAXException("RIF: 'args' element must be preceeded by an 'op' element within an 'Expr' or 'Atom' element.");
								descent.push(Element.TERM_ARGS);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta', 'op' or 'args' expected, '"+localName+"' found.");
						}
						break;
					case OP:
						switch (localName.charAt(0)){
							case 'C':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Const' element must be the sole child of an 'op' element.");
								descent.push(Element.CONST);
								lastSibling.push(null);
								
								if (atts.getValue("type").equals("&rif;iri")){
									currentConst = new RIFIRIConst();
								}else{
									currentConst = new RIFStringConst();
								}
								break;
							default:
								throw new SAXException("RIF: 'Const' expected, '"+localName+"' found.");
						}
						break;
					case GROUND_ARGS:
						lastSibling.pop();
						lastSibling.push(null);
						d = startGROUNDTERM(localName,atts);
						if(d instanceof RIFDatum)
							currentAtom.addArg((RIFDatum) d);
						else
							throw new SAXException("RIF: 'arg' elements must contain only 'Const' and 'External' elements, i.e. base terms.");
						if (d instanceof RIFList){
							currentList.push((RIFList) d);
						}
						break;
					case TERM_ARGS:
						lastSibling.pop();
						lastSibling.push(null);
						d = startTERM(localName,atts);
						if(d instanceof RIFDatum)
							currentAtom.addArg((RIFDatum) d);
						else
							throw new SAXException("RIF: 'arg' elements must contain only 'Var', 'Const' and 'External' elements, i.e. base terms.");
						if (d instanceof RIFList){
							currentList.push((RIFList) d);
						}
						break;
					case FORMULA_EXTERNAL:
						switch (localName.charAt(0)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'id' element, if it exists, must be the first child of any element.");
								descent.push(Element.ID);
								lastSibling.push(null);
								break;
							case 'm':
								if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
								descent.push(Element.META);
								lastSibling.push(null);
								break;
							case 'c':
								if (lastSibling.peek() == Element.FORMULA_CONTENT) throw new SAXException("RIF: 'content' element must be the unique in an 'External' element.");
								descent.push(Element.FORMULA_CONTENT);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'id', 'meta' or 'content' expected, '"+localName+"' found.");
						}
						break;
					case FORMULA_CONTENT:
						switch (localName.charAt(0)){
							case 'A':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element must be the sole child of an 'content' element.");
								descent.push(Element.ATOM);
								lastSibling.push(null);
								
								currentAtom = new RIFAtom();
								currentExternal.peek().setCommand(currentAtom);
								
								break;
							default:
								throw new SAXException("RIF: 'Atom' expected, '"+localName+"' found.");
						}
						break;
						
					case THEN:
						switch (localName.charAt(0)){
							case 'A':
								switch (localName.charAt(1)){
									case 't':
										if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element, if it exists, must be the sole child of a 'then' element.");
										descent.push(Element.ATOM);
										lastSibling.push(null);
										
										currentAtom = new RIFAtom();
										currentRule.addAtomicToHead(currentAtom);
										
										break;
									case 'n':
										if (lastSibling.peek() != null) throw new SAXException("RIF: 'And' element, if it exists, must be the sole child of a 'then' element.");
										descent.push(Element.THEN_AND);
										lastSibling.push(null);
										break;
									default:
										throw new SAXException("RIF: 'And', 'Frame' or 'Atom' expected, '"+localName+"' found.");
								}
								break;
							case 'F':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of a 'then' element.");
								descent.push(Element.FRAME);
								lastSibling.push(null);
								
								currentFrame = new RIFFrame();
								currentRule.addAtomicToHead(currentFrame);
								
								break;
							default:
								throw new SAXException("RIF: 'And', 'Frame' or 'Atom' expected, '"+localName+"' found.");
						}
						break;
					case THEN_AND:
						switch (localName.charAt(0)){
							case 'f':
								descent.push(Element.THEN_AND_FORMULA);
								lastSibling.push(null);
								break;
							default:
								throw new SAXException("RIF: 'formula' expected, '"+localName+"' found.");
						}
						break;
					case THEN_AND_FORMULA:
						switch (localName.charAt(0)){
							case 'A':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
								descent.push(Element.ATOM);
								lastSibling.push(null);
								
								currentAtom = new RIFAtom();
								currentRule.addAtomicToHead(currentAtom);
								
								break;
							case 'F':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
								descent.push(Element.FRAME);
								lastSibling.push(null);
								
								currentFrame = new RIFFrame();
								currentRule.addAtomicToHead(currentFrame);
								
								break;
							default:
								throw new SAXException("RIF: 'Frame' or 'Atom' expected, '"+localName+"' found.");
						}
						break;
						
					default:
						throw new SAXException("RIF: '"+descent.peek().toString()+"' element must not contain child elements.");
				}
			}
		}
		
		private void pushToFormula(RIFFormula f){
			if (currentFormula.isEmpty()){
				currentRule.setBody(f);
			}else{
				currentFormula.peek().addFormula(f);
			}
		}
		
		private void startFORMULA(String localName) throws SAXException{
			switch (localName.charAt(0)){
				case 'A':
					switch (localName.charAt(1)){
						case 'n':
							if (lastSibling.peek() != null) throw new SAXException("RIF: 'And' element, if it exists, must be the sole child of an element.");
							descent.push(Element.AND);
							lastSibling.push(null);
							
							RIFAnd an = new RIFAnd();
							pushToFormula(an);
							currentFormula.push(an);
							
							break;
						case 't':
							if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element, if it exists, must be the sole child of an element.");
							descent.push(Element.ATOM);
							lastSibling.push(null);
							
							currentAtom = new RIFAtom();
							pushToFormula(currentAtom);
							
							break;
						default:
							throw new SAXException("RIF: 'And', 'Or', 'Exists', 'Equal', 'Member', 'Frame', 'Atom' or 'External' expected, '"+localName+"' found.");
					}
					break;
				case 'O':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'Or' element, if it exists, must be the sole child of an element.");
					descent.push(Element.OR);
					lastSibling.push(null);
					
					RIFOr o = new RIFOr();
					pushToFormula(o);
					currentFormula.push(o);
					
					break;
				case 'E':
					switch (localName.charAt(1)){
						case 'x':
							switch (localName.charAt(2)){
								case 'i':
									if (lastSibling.peek() != null) throw new SAXException("RIF: 'Exists' element, if it exists, must be the sole child of an element.");
									descent.push(Element.EXISTS);
									lastSibling.push(null);
									
									RIFExists exists = new RIFExists();
									pushToFormula(exists);
									currentFormula.push(exists);
									
									break;
								case 't':
									if (lastSibling.peek() != null) throw new SAXException("RIF: 'External' element, if it exists, must be the sole child of an element.");
									descent.push(Element.FORMULA_EXTERNAL);
									lastSibling.push(null);
									
									RIFExternal ext = new RIFExternal();
									pushToFormula(ext);
									currentExternal.push(ext);
									
									break;
								default:
									throw new SAXException("RIF: 'And', 'Or', 'Exists', 'Equal', 'Member', 'Frame', 'Atom' or 'External' expected, '"+localName+"' found.");
							}
							break;
						case 'q':
							if (lastSibling.peek() != null) throw new SAXException("RIF: 'Equal' element, if it exists, must be the sole child of an element.");
							descent.push(Element.EQUAL);
							lastSibling.push(null);
							
							currentEqual = new RIFEqual();
							pushToFormula(currentEqual);
							
							break;
						default:
							throw new SAXException("RIF: 'And', 'Or', 'Exists', 'Equal', 'Member', 'Frame', 'Atom' or 'External' expected, '"+localName+"' found.");
					}
					break;
				case 'M':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'Member' element, if it exists, must be the sole child of an element.");
					descent.push(Element.MEMBER);
					lastSibling.push(null);
					
					currentMember = new RIFMember();
					pushToFormula(currentMember);
					
					break;
				case 'F':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of an element.");
					descent.push(Element.FRAME);
					lastSibling.push(null);
					
					currentFrame = new RIFFrame();
					pushToFormula(currentFrame);
					
					break;
				default:
					throw new SAXException("RIF: 'And', 'Or', 'Exists', 'Equal', 'Member', 'Frame', 'Atom' or 'External' expected, '"+localName+"' found.");
			}
		}
		
		private RIFData startTERM(String localName, Attributes atts) throws SAXException{
			switch (localName.charAt(0)){
				case 'C':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'Const' element, if it exists, must be the sole child of an element.");
					descent.push(Element.CONST);
					lastSibling.push(null);
					
					if (atts.getValue("type") != null){
//System.out.println(atts.getValue("type"));
						URI uri = findURI(atts.getValue("type"));
						if (uri.toString().equals(RIFIRIConst.datatype)){
							currentConst = new RIFIRIConst();
						}else if (uri.toString().equals(RIFStringConst.datatype)){
							currentConst = new RIFStringConst();
						}else{
							currentConst = new RIFUnrecognisedConst(uri);
						}
					}else{
						currentConst = new RIFStringConst();
					}
					return currentConst;
				case 'V':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'Var' element, if it exists, must be the sole child of an element.");
					descent.push(Element.VAR);
					lastSibling.push(null);
					
					currentVar = new RIFVar();
					return currentVar;
				case 'L':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'List' element, if it exists, must be the sole child of an element.");
					descent.push(Element.LIST);
					lastSibling.push(null);
					
					return new RIFList();
				case 'E':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'External' element, if it exists, must be the sole child of an element.");
					descent.push(Element.TERM_EXTERNAL);
					lastSibling.push(null);
					
					currentExternal.push(new RIFExternal());
					return currentExternal.peek();
				default:
					throw new SAXException("RIF: 'Const', 'Var', 'List' or 'External' expected, '"+localName+"' found.");
			}
		}
		
		private RIFData startGROUNDTERM(String localName, Attributes atts) throws SAXException{
			switch (localName.charAt(0)){
				case 'C':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'Const' element, if it exists, must be the sole child of an element.");
					descent.push(Element.CONST);
					lastSibling.push(null);
					
					if (atts.getValue("type") != null
							&& atts.getValue("type").equals(RIFIRIConst.datatype)){
//System.out.println(atts.getValue("type"));
							currentConst = new RIFIRIConst();
						}else{
							currentConst = new RIFStringConst();
						}
					return currentConst;
				case 'L':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'List' element, if it exists, must be the sole child of an element.");
					descent.push(Element.LIST);
					lastSibling.push(null);

					return new RIFList();
				case 'E':
					if (lastSibling.peek() != null) throw new SAXException("RIF: 'External' element, if it exists, must be the sole child of an element.");
					descent.push(Element.GROUND_EXTERNAL);
					lastSibling.push(null);

					currentExternal.push(new RIFExternal());
					return currentExternal.peek();
				default:
					throw new SAXException("RIF: 'Const', 'List' or 'External' expected, '"+localName+"' found.");
			}
		}
		
		//   CONTENT HANDLING
		
		@Override
		public void characters(char[] chars, int start, int length) throws SAXException{
			StringBuilder filteredChars = new StringBuilder();
			for (int i = start; i < start + length; i++)
				if (chars[i] != '\n' && chars[i] != '\r')
					filteredChars.append(chars[i]);
			if (filteredChars.length() > 0)
				try {
					partialContent.append(filteredChars);
				} catch (NullPointerException e){
					partialContent = filteredChars;
				}
		}
		
		private void handleContent() throws SAXException {
			if (partialContent != null){
				String content = partialContent.toString();
				partialContent = null;
				
				if (!descent.isEmpty())
					switch (descent.peek()){
						case CONST:
							if (currentConst instanceof RIFIRIConst)
								((RIFIRIConst) currentConst).setData(findURI(content));
							else
								((RIFStringConst) currentConst).setData(content);
							break;
						case IRICONST:
							break;
							
						case LOCATION:
							currentLocation = findURI(content);
							break;
						case PROFILE:
							currentProfile = findURI(content);
							break;
						
						case VAR:
						case NAME:
							currentVar.setName(content);
							break;
						default:
					}
			}
		}
		
		//   ELEMENT END HANDLING
		
		@Override
		public void endElement(		String uri,
									String localName,
								String qName)
				throws SAXException {
			handleContent();
			
			if (localName == null || localName.equals(""))
				localName = qName;
			
//System.out.println("/"+localName);
			
			if (!descent.isEmpty()){
				if (!localName.equals(descent.peek().toString()))
					throw new SAXException("RIF: Closing tag mismatch on '"+descent.peek().toString()+"'.");
				
				switch (descent.peek()){
					case ITEMS:
						currentList.pop();
						break;
					case FORMULA_EXTERNAL:
					case TERM_EXTERNAL:
					case GROUND_EXTERNAL:
						currentExternal.pop();
						break;
					case GROUP:
						if (currentGroup.isEmpty())
							currentGroup = null;
						else
							currentGroup.pop();
						break;
					case AND:
					case OR:
					case EXISTS:
						currentFormula.pop();
						break;
					case IMPORT:
						ruleSet.addImport(currentLocation, currentProfile);
						currentLocation = null;
						currentProfile = null;
						break;
					default:
				}
				
				lastSibling.pop();
				if (!lastSibling.isEmpty()){
					lastSibling.pop();
					lastSibling.push(descent.pop());
				}
			}
		}
		
	}
	
}