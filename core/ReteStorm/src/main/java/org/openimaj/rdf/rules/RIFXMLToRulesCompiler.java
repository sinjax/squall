package org.openimaj.rdf.rules;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Converts a set of rules expressed in RIF/XML to a set of rules in a different format.
 * @author David Monks <david.monks@zepler.net>
 */
public class RIFXMLToRulesCompiler extends DefaultHandler {

	/**
	 * 
	 * @param rulesURI -
	 * 			The URI of the rule set whose rules are to be converted.
	 * @param ruleConstructor -
	 * 			The Object that will construct the set of rules produced by the converter.
	 * @return 
	 * 			True if the rule set was successfully converted, false otherwise.
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static boolean compile(URI rulesURI, RuleConstructor ruleConstructor) throws IOException, SAXException {
		//Fetch the rules from the rulesURI and load into a SAX parser.
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    
	    SAXParser saxParser;
		try {
			saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
		    xmlReader.setContentHandler(new RIFXMLToRulesCompiler(ruleConstructor));
		    xmlReader.parse(new InputSource(rulesURI.toASCIIString()));
			
			return true;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private enum Element {
		//IRIMETA
		ID("id")
		,
			IRICONST("Const")
			,
		META("meta")
		,
//			Generic FRAME
//			,
			META_AND("And")
			,
				META_FORMULA("formula")
				,
//					Generic FRAME
//					,
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
									NAME("Name")
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
											FORMULA("formula")
											,
//												GENERIC_FORMULA
//												,
										OR("Or")
										,
//											FORMULA	//formula
//											,
//												GENERIC_FORMULA
//												,
										EXISTS("Exists")
										,
//											DECLARE	//declare
//											,
//												Generic VAR
//												,
//											FORMULA	//formula
//											,
//												GENERIC_FORMULA
//												,
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
														GROUND_EXTERNAL("External")	// <- Generic GROUND_EXTERNAL
														,
															GROUND_CONTENT("content")
															,
																GROUND_EXPR("Expr")
																,
//																	OP	//op
//																	,
//																		Generic CONST
//																		,
																	GROUND_ARGS("args")
																	,
//																		Generic CONST
//																		,
//																		Generic LIST
//																		,
//																		Generic GROUND_EXTERNAL
//																		,
																	
												TERM_EXTERNAL("External")	// <- Generic TERM_EXTERNAL
												,
													TERM_CONTENT("content")
													,
														TERM_EXPR("Expr")
														,
//															OP	//op
//															,
//																Generic CONST
//																,
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
//												Generic TERM_EXTERNAL
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
//												Generic TERM_EXTERNAL
//												,
											CLASS("class")
											,
//												Generic CONST
//												,
//												Generic VAR
//												,
//												Generic LIST
//												,
//												Generic TERM_EXTERNAL
//												,
										FORMULA_EXTERNAL("External")
										,
											FORMULA_CONTENT("content")
											,
												ATOM("Atom")
												,
													OP("op")
													,
//														Generic CONST
//														,
													TERM_ARGS("args")	// <- Generic ARGS
													,
//														Generic CONST
//														,
//														Generic VAR
//														,
//														Generic LIST
//														,
//														Generic TERM_EXTERNAL
//														,
									THEN("then")
									,
//										ATOM	//Atom
//										,
//											OP	//op
//											,
//												Generic CONST
//												,
//											Generic ARGS
//											,
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
//												Generic TERM_EXTERNAL
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
//													Generic TERM_EXTERNAL
//													,
//												}x2
										THEN_AND("And")
										,
											THEN_AND_FORMULA("formula")
//											,
//												ATOM	//Atom
//												,
//													OP	//op
//													,
//														Generic CONST
//														,
//													Generic ARGS
//													,
//												Generic FRAME
//												,
//						,
//						Generic IMPLIES
//						,
		;
		
		private final String tag;
		
		private Element(String tag){
			this.tag = tag;
		}
		
		public String toString(){
			return tag;
		}
	}
	
	private RuleConstructor ruleConstructor;
	
	private RIFXMLToRulesCompiler (RuleConstructor rc){
		this.ruleConstructor = rc;
	}
	
	private Stack<Element> descent;
	private Stack<Element> lastSibling;
	
	private StringBuilder partialContent;
	
	//   !ENTITY HANDLING
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId){
		try {
			ruleConstructor.addPrefix(publicId, new URI(systemId));
		} catch (URISyntaxException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		
		InputSource returnable = new InputSource(systemId);
		returnable.setPublicId(publicId);
		return returnable;
	}
	
	//   DOCUMENT HANDLING
	
	@Override
	public void startDocument() throws SAXException {
		descent = new Stack<Element>();
		lastSibling = new Stack<Element>();
		
		partialContent = null;
	}
	
	@Override
	public void endDocument() throws SAXException {
		
	}
	
	//   ELEMENT START HANDLING
	
	@Override
	public void startElement(		String namespaceURI,
            				 String localName,
            				 		String qName, 
            				 		Attributes atts)
           throws SAXException {
		if (partialContent != null) handleContent();
				
		switch (descent.peek()){
			case ID:
				switch (localName.charAt(0)){
					case 'C':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Const' element, if it exists, must be the first child of an 'id' element.");
						descent.push(Element.IRICONST);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case IRICONST:
				throw new SAXException("RIF: 'Const' element must not contain child elements when within 'id' element.");
			case META:
				switch (localName.charAt(0)){
					case 'F':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' elements must be the sole child of a 'meta' element.");
						descent.push(Element.FRAME);
						lastSibling.push(null);
						break;
					case 'A':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'And' elements must be the sole child of a 'meta' element.");
						descent.push(Element.META_AND);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case META_AND:
				switch (localName.charAt(0)){
					case 'f':
						descent.push(Element.META_FORMULA);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case META_FORMULA:
				switch (localName.charAt(0)){
					case 'F':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' elements must be the sole child of a 'formula' element when within a 'meta'.");
						descent.push(Element.FRAME);
						lastSibling.push(null);
						break;
					default:
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
				}
				break;
			case LOCATION:
				throw new SAXException("RIF: 'location' element must not contain child elements.");
			case PROFILE:
				throw new SAXException("RIF: 'profile' element must not contain child elements.");
				
			case PAYLOAD:
				switch (localName.charAt(0)){
					case 'G':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Group' element must be within a 'payload' element.");
						descent.push(Element.GROUP);
						lastSibling.push(null);
						break;
					default:
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
				}
				break;
			case SENTENCE:
				switch (localName.charAt(0)){
					case 'G':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Group' element must be unique within a 'sentence' element.");
						descent.push(Element.GROUP);
						lastSibling.push(null);
						break;
					case 'F':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Forall' element must be unique within a 'sentence' element.");
						descent.push(Element.FOR_ALL);
						lastSibling.push(null);
						break;
					case 'I':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Implies' element must be unique within a 'sentence' element.");
						descent.push(Element.IMPLIES);
						lastSibling.push(null);
						break;
					default:
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
						descent.push(Element.DECLARE);
						lastSibling.push(null);
						break;
					case 'f':
						if (lastSibling.peek() != Element.DECLARE) throw new SAXException("RIF: 'formula' element must be preceeded by at least one 'declare' element when within a 'Forall' element.");
						descent.push(Element.FOR_ALL_FORMULA);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case DECLARE:
				switch (localName.charAt(0)){
					case 'V':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Var' elements must be the sole child of a 'declare' element.");
						descent.push(Element.VAR);
						lastSibling.push(null);
						break;
					default:
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
				}
				break;
			case NAME:
				throw new SAXException("RIF: 'Name' element must not contain child elements.");
			case FOR_ALL_FORMULA:
				switch (localName.charAt(0)){
					case 'I':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Implies' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
						descent.push(Element.IMPLIES);
						lastSibling.push(null);
						break;
					case 'A':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
						descent.push(Element.ATOM);
						lastSibling.push(null);
						break;
					case 'F':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
						descent.push(Element.FRAME);
						lastSibling.push(null);
						break;
					default:
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
					break;
				default:
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
						if (lastSibling.peek() != Element.OBJECT) throw new SAXException("RIF: 'slot' element must be preceeded by an 'object' element within a 'Frame' element.");
						descent.push(Element.SLOT);
						descent.push(Element.SLOT_FIRST);
						lastSibling.push(null);
						lastSibling.push(null);
						break;
					default:
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
				}
				break;
			case SLOT_FIRST:
				if (lastSibling.peek() == null){
					startTERM(localName);
					break;
				}
				descent.pop();
				lastSibling.pop();
			case OBJECT:
			case SLOT:
			case RIGHT:
			case LEFT:
			case INSTANCE:
			case CLASS:
				startTERM(localName);
				break;
			case CONST:
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
						}
						break;
					case 'm':
						if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
						descent.push(Element.META);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case ITEMS:
				lastSibling.pop();
				lastSibling.push(null);
				startGROUNDTERM(localName);
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
				}
				break;
			case GROUND_CONTENT:
				switch (localName.charAt(0)){
					case 'E':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Expr' element must be the sole child of a 'content' element.");
						descent.push(Element.GROUND_EXPR);
						lastSibling.push(null);
						break;
					default:
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
				}
				break;
			case TERM_CONTENT:
				switch (localName.charAt(0)){
					case 'E':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Expr' element must be the sole child of a 'content' element.");
						descent.push(Element.TERM_EXPR);
						lastSibling.push(null);
						break;
					default:
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
					case 'a':
						if (lastSibling.peek() != Element.OP) throw new SAXException("RIF: 'args' element must be preceeded by an 'op' element within an 'Expr' element.");
						descent.push(Element.GROUND_ARGS);
						lastSibling.push(null);
					default:
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
					case 'a':
						if (lastSibling.peek() != Element.OP) throw new SAXException("RIF: 'args' element must be preceeded by an 'op' element within an 'Expr' or 'Atom' element.");
						descent.push(Element.TERM_ARGS);
						lastSibling.push(null);
					default:
				}
				break;
			case OP:
				switch (localName.charAt(0)){
					case 'C':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Const' element must be the sole child of an 'op' element.");
						descent.push(Element.CONST);
						lastSibling.push(null);
					default:
				}
				break;
			case GROUND_ARGS:
				lastSibling.pop();
				lastSibling.push(null);
				startGROUNDTERM(localName);
				break;
			case TERM_ARGS:
				lastSibling.pop();
				lastSibling.push(null);
				startTERM(localName);
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
				}
				break;
			case FORMULA_CONTENT:
				switch (localName.charAt(0)){
					case 'A':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element must be the sole child of an 'content' element.");
						descent.push(Element.ATOM);
						lastSibling.push(null);
					default:
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
								break;
							case 'n':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'And' element, if it exists, must be the sole child of a 'then' element.");
								descent.push(Element.THEN_AND);
								lastSibling.push(null);
								break;
							default:
						}
						break;
					case 'F':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of a 'then' element.");
						descent.push(Element.FRAME);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case THEN_AND:
				switch (localName.charAt(0)){
					case 'f':
						descent.push(Element.THEN_AND_FORMULA);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case THEN_AND_FORMULA:
				switch (localName.charAt(0)){
					case 'A':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
						descent.push(Element.ATOM);
						lastSibling.push(null);
						break;
					case 'F':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
						descent.push(Element.FRAME);
						lastSibling.push(null);
						break;
					default:
				}
				break;
				
			default:
				if (localName.equals("Document")){
					descent.push(Element.DOCUMENT);
					lastSibling.push(null);
				}else{
					
				}
		}
	}
	
	private void startFORMULA(String localName) throws SAXException{
		switch (localName.charAt(0)){
			case 'A':
				switch (localName.charAt(0)){
					case 'n':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'And' element, if it exists, must be the sole child of an element.");
						descent.push(Element.AND);
						lastSibling.push(null);
						break;
					case 't':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Atom' element, if it exists, must be the sole child of an element.");
						descent.push(Element.ATOM);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case 'O':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'Or' element, if it exists, must be the sole child of an element.");
				descent.push(Element.OR);
				lastSibling.push(null);
				break;
			case 'E':
				switch (localName.charAt(1)){
					case 'x':
						switch (localName.charAt(2)){
							case 'i':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'Exists' element, if it exists, must be the sole child of an element.");
								descent.push(Element.EXISTS);
								lastSibling.push(null);
								break;
							case 't':
								if (lastSibling.peek() != null) throw new SAXException("RIF: 'External' element, if it exists, must be the sole child of an element.");
								descent.push(Element.FORMULA_EXTERNAL);
								lastSibling.push(null);
								break;
							default:
						}
						break;
					case 'q':
						if (lastSibling.peek() != null) throw new SAXException("RIF: 'Equal' element, if it exists, must be the sole child of an element.");
						descent.push(Element.EQUAL);
						lastSibling.push(null);
						break;
					default:
				}
				break;
			case 'M':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'Member' element, if it exists, must be the sole child of an element.");
				descent.push(Element.MEMBER);
				lastSibling.push(null);
				break;
			case 'F':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'Frame' element, if it exists, must be the sole child of an element.");
				descent.push(Element.FRAME);
				lastSibling.push(null);
				break;
			default:
		}
	}
	
	private void startTERM(String localName) throws SAXException{
		switch (localName.charAt(0)){
			case 'C':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'Const' element, if it exists, must be the sole child of an element.");
				descent.push(Element.CONST);
				lastSibling.push(null);
				break;
			case 'V':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'Var' element, if it exists, must be the sole child of an element.");
				descent.push(Element.VAR);
				lastSibling.push(null);
				break;
			case 'L':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'List' element, if it exists, must be the sole child of an element.");
				descent.push(Element.LIST);
				lastSibling.push(null);
				break;
			case 'E':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'External' element, if it exists, must be the sole child of an element.");
				descent.push(Element.TERM_EXTERNAL);
				lastSibling.push(null);
				break;
			default:
		}
	}
	
	private void startGROUNDTERM(String localName) throws SAXException{
		switch (localName.charAt(0)){
			case 'C':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'Const' element, if it exists, must be the sole child of an element.");
				descent.push(Element.CONST);
				lastSibling.push(null);
				break;
			case 'L':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'List' element, if it exists, must be the sole child of an element.");
				descent.push(Element.LIST);
				lastSibling.push(null);
				break;
			case 'E':
				if (lastSibling.peek() != null) throw new SAXException("RIF: 'External' element, if it exists, must be the sole child of an element.");
				descent.push(Element.GROUND_EXTERNAL);
				lastSibling.push(null);
				break;
			default:
		}
	}
	
	//   CONTENT HANDLING
	
	@Override
	public void characters(char[] chars, int arg0, int arg1){
		try {
			for (int i = arg0; i < arg0 + arg1; i++)
				partialContent.append(chars[i]);
		} catch (NullPointerException e){
			partialContent = new StringBuilder();
			for (int i = arg0; i < arg0 + arg1; i++)
				partialContent.append(chars[i]);
		}
	}
	
	private void handleContent() throws SAXException {
		String content = partialContent.toString();
		partialContent = null;
		
		switch (descent.peek()){
		case CONST:
			break;
		case IRICONST:
			break;
			
		case LOCATION:
			break;
		case PROFILE:
			break;
		
		case VAR:
			break;
		case NAME:
			break;
		default:
			throw new SAXException("RIF: Did not expect characters value for element "+descent.peek().toString());
		}
	}
	
	//   ELEMENT END HANDLING
	
	public void endElement(		String uri,
							String localName,
								String qName)
			throws SAXException {
		if (partialContent != null) handleContent();
		
		if (!localName.equals(descent.peek().toString()))
			throw new SAXException("RIF: Closing tag mismatch on '"+descent.peek().toString()+"'.");
		
		lastSibling.pop();
		lastSibling.pop();
		lastSibling.push(descent.pop());
	}
	
}
