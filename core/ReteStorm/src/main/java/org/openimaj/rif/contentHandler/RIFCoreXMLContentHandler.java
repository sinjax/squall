package org.openimaj.rif.contentHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.openimaj.rif.conditions.atomic.RIFAtom;
import org.openimaj.rif.conditions.atomic.RIFFrame;
import org.openimaj.rif.conditions.data.RIFConst;
import org.openimaj.rif.conditions.data.RIFData;
import org.openimaj.rif.conditions.data.RIFDatum;
import org.openimaj.rif.conditions.data.RIFExpr;
import org.openimaj.rif.conditions.data.RIFExternalExpr;
import org.openimaj.rif.conditions.data.RIFIRIConst;
import org.openimaj.rif.conditions.data.RIFList;
import org.openimaj.rif.conditions.data.RIFLocalConst;
import org.openimaj.rif.conditions.data.RIFStringConst;
import org.openimaj.rif.conditions.data.RIFTypedConst;
import org.openimaj.rif.conditions.data.RIFURIConst;
import org.openimaj.rif.conditions.data.RIFVar;
import org.openimaj.rif.conditions.formula.RIFAnd;
import org.openimaj.rif.conditions.formula.RIFEqual;
import org.openimaj.rif.conditions.formula.RIFExists;
import org.openimaj.rif.conditions.formula.RIFExternalValue;
import org.openimaj.rif.conditions.formula.RIFFormula;
import org.openimaj.rif.conditions.formula.RIFMember;
import org.openimaj.rif.conditions.formula.RIFOr;
import org.openimaj.rif.rules.RIFForAll;
import org.openimaj.rif.rules.RIFGroup;
import org.openimaj.rif.rules.RIFRule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public class RIFCoreXMLContentHandler extends RIFXMLContentHandler {
	
	/**
	 * 
	 */
	public RIFCoreXMLContentHandler(){
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
				
				return;
			}
		}else{
			switch (descent.peek()){
				case ID:
					switch (localName.charAt(0)){
						case 'C':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Const' element, if it exists, must be the first child of an 'id' element.");
							descent.push(Element.IRICONST);
							lastSibling.push(null);
							
							if (atts.getValue("type") != null){
//System.out.println(atts.getValue("type"));
								URI uri = findURI(atts.getValue("type"));
								if (uri.toString().equals(RIFIRIConst.datatype)){
									currentConst = new RIFIRIConst();
								}else{
									throw new SAXException("RIF: 'id' elements can only contain rif:iri typed 'Const' elements.");
								}
							}else{
								currentConst = new RIFIRIConst();
							}
							this.currentMetaHolder.setID((RIFIRIConst) this.currentConst);
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Const' expected, '"+localName+"' found.");
					}
					break;
				case META:
					switch (localName.charAt(0)){
						case 'F':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Frame' elements must be the sole child of a 'meta' element.");
							descent.push(Element.FRAME);
							lastSibling.push(null);
							
							this.currentFrame = new RIFFrame();
							this.currentMetaHolder.setMetadata(this.currentFrame);
							
							break;
						case 'A':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'And' elements must be the sole child of a 'meta' element.");
							descent.push(Element.META_AND);
							lastSibling.push(null);
							
							this.currentFormula.push(new RIFAnd());
							this.currentMetaHolder.setMetadata(this.currentFormula.peek());
							
							break;
						default:
							throw new SAXException("RIF-Core: 'And' or 'Frame' expected, '"+localName+"' found.");
					}
					break;
				case META_AND:
					switch (localName.charAt(0)){
						case 'f':
							descent.push(Element.META_FORMULA);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'formula' expected, '"+localName+"' found.");
					}
					break;
				case META_FORMULA:
					switch (localName.charAt(0)){
						case 'F':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Frame' elements must be the sole child of a 'formula' element when within a 'meta'.");
							descent.push(Element.FRAME);
							lastSibling.push(null);
							
							currentFrame = new RIFFrame();
							currentFormula.peek().addFormula(currentFrame);
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Frame' expected, '"+localName+"' found.");
					}
					break;
					
				case DOCUMENT:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'd':
							if (lastSibling.peek() == Element.PAYLOAD) throw new SAXException("RIF-Core: All 'directive' elements must preceed any 'payload' element.");
							descent.push(Element.DIRECTIVE);
							lastSibling.push(null);
							break;
						case 'p':
							if (lastSibling.peek() == Element.PAYLOAD) throw new SAXException("RIF-Core: 'payload' elements must be unique within a 'directive' element.");
							descent.push(Element.PAYLOAD);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta', 'directive' or 'payload' expected, '"+localName+"' found.");
					}
					break;
				case DIRECTIVE:
					switch (localName.charAt(0)){
						case 'I':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Import' elements must be the sole child of a 'directive' element.");
							descent.push(Element.IMPORT);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'Import' expected, '"+localName+"' found.");
					}
					break;
				case IMPORT:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'l':
							if (lastSibling.peek() == Element.LOCATION) throw new SAXException("RIF-Core: 'location' element must be unique within an 'Import' element.");
							if (lastSibling.peek() == Element.PROFILE) throw new SAXException("RIF-Core: 'location' element must preceed any 'profile' element within an 'Import' element.");
							descent.push(Element.LOCATION);
							lastSibling.push(null);
							break;
						case 'p':
							if (lastSibling.peek() != Element.LOCATION) throw new SAXException("RIF-Core: 'profile' element, if it exists, must be preceeded by a 'location' element.");
							descent.push(Element.PROFILE);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta', 'location' or 'profile' expected, '"+localName+"' found.");
					}
					break;
					
				case PAYLOAD:
					switch (localName.charAt(0)){
						case 'G':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Group' element must be within a 'payload' element.");
							descent.push(Element.GROUP);
							lastSibling.push(null);
							
							if (currentGroup == null){
								currentGroup = new Stack<RIFGroup>();
								this.ruleSet.addRootGroup(new RIFGroup());
								currentGroup.push(this.ruleSet.getRootGroup());
							}else
								throw new SAXException("RIF-Core: Should not have constructed the Group stack before first Group in payload.");
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Group' expected, '"+localName+"' found.");
					}
					break;
				case GROUP:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 's':
							descent.push(Element.SENTENCE);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta' or 'sentence' expected, '"+localName+"' found.");
					}
					break;
				case SENTENCE:
					switch (localName.charAt(0)){
						case 'G':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Group' element must be the sole child of a 'sentence' element.");
							descent.push(Element.GROUP);
							lastSibling.push(null);
							
							RIFGroup g = new RIFGroup();
							if (currentGroup.isEmpty())
								this.ruleSet.addRootGroup(g);
							else
								currentGroup.peek().addSentence(g);
							currentGroup.push(g);
							
							break;
						case 'F':
							switch(localName.charAt(1)){
								case 'o':
									if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Forall' element must be the sole child of a 'sentence' element.");
									descent.push(Element.FOR_ALL);
									lastSibling.push(null);
									
									names.push(new ArrayList<String>());
									
									currentForAll = new RIFForAll();
									if (currentGroup.isEmpty())
										throw new SAXException("RIF-Core: 'Forall' element cannot be the child of a 'payload' element.");
									else
										currentGroup.peek().addSentence(currentForAll);
									
									break;
								case 'r':
									if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Frame' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
									descent.push(Element.FRAME);
									lastSibling.push(null);
									
									currentFrame = new RIFFrame();
									if (currentGroup.isEmpty())
										throw new SAXException("RIF-Core: 'Frame' element cannot be the child of a 'payload' element.");
									else
										currentGroup.peek().addSentence(currentFrame);
									
									break;
								default:
									throw new SAXException("RIF-Core: 'Group', 'ForAll', 'Frame', 'Atom' or 'Implies' expected, '"+localName+"' found.");
							}
							
							break;
						case 'I':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Implies' element must be the sole child of a 'sentence' element.");
							descent.push(Element.IMPLIES);
							lastSibling.push(null);
							
							currentRule = new RIFRule();
							if (currentGroup.isEmpty())
								throw new SAXException("RIF-Core: 'Implies' element cannot be the child of a 'payload' element.");
							else
								currentGroup.peek().addSentence(currentRule);
							
							break;
						case 'A':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Atom' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
							descent.push(Element.ATOM);
							lastSibling.push(null);
							
							currentAtom = new RIFAtom();
							if (currentGroup.isEmpty())
								throw new SAXException("RIF-Core: 'Atom' element cannot be the child of a 'payload' element.");
							else
								currentGroup.peek().addSentence(currentAtom);
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Group', 'ForAll', 'Frame', 'Atom' or 'Implies' expected, '"+localName+"' found.");
					}
					break;
				case FOR_ALL:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'd':
							if (lastSibling.peek() == Element.FOR_ALL_FORMULA) throw new SAXException("RIF-Core: 'declare' element must be stated before any 'formula' element when within a 'Forall' element.");
							descent.push(Element.DECLARE);
							lastSibling.push(null);
							break;
						case 'f':
							if (lastSibling.peek() != Element.DECLARE) throw new SAXException("RIF-Core: 'formula' element must be preceeded by at least one 'declare' element when within a 'Forall' element.");
							descent.push(Element.FOR_ALL_FORMULA);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta', 'declare' or 'formula' expected, '"+localName+"' found.");
					}
					break;
				case DECLARE:
					switch (localName.charAt(0)){
						case 'V':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Var' elements must be the sole child of a 'declare' element.");
							descent.push(Element.VAR);
							lastSibling.push(null);
							
							currentVar = new RIFVar();
							if (currentFormula.isEmpty())
								currentForAll.addUniversalVar(currentVar);
							else
								try {
									((RIFExists) currentFormula.peek()).addExistentialVar(currentVar);
								} catch (ClassCastException e) {
									throw new SAXException("RIF-Core: variable declarations can only occur as direct children of 'ForAll' or 'Exists' elements", e);
								}
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Var' expected, '"+localName+"' found.");
					}
					break;
				case VAR:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id' or 'meta' expected, '"+localName+"' found.");
					}
					break;
				case FOR_ALL_FORMULA:
					switch (localName.charAt(0)){
						case 'I':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Implies' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
							descent.push(Element.IMPLIES);
							lastSibling.push(null);
							
							currentRule = new RIFRule();
							currentForAll.setStatement(currentRule);
							
							break;
						case 'A':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Atom' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
							descent.push(Element.ATOM);
							lastSibling.push(null);
							
							currentAtom = new RIFAtom();
							currentForAll.setStatement(currentAtom);
							
							break;
						case 'F':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Frame' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
							descent.push(Element.FRAME);
							lastSibling.push(null);
							
							currentFrame = new RIFFrame();
							currentForAll.setStatement(currentFrame);
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Implies', 'Atom' or 'Frame' expected, '"+localName+"' found.");
					}
					break;
				case IMPLIES:
					switch (localName.charAt(0)){
						case 'i':
							switch (localName.charAt(1)){
								case 'd':
									if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
									descent.push(Element.ID);
									lastSibling.push(null);
									break;
								case 'f':
									if (lastSibling.peek() == Element.IF) throw new SAXException("RIF-Core: 'if' element must be unique within an 'Implies' element.");
									if (lastSibling.peek() == Element.THEN) throw new SAXException("RIF-Core: 'if' element must preceed any 'then' element within an 'Implies' element.");
									descent.push(Element.IF);
									lastSibling.push(null);
									break;
								default:
									throw new SAXException("RIF-Core: 'id', 'meta', 'if' or 'then' expected, '"+localName+"' found.");
							}
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 't':
							if (lastSibling.peek() != Element.IF) throw new SAXException("RIF-Core: 'then' element must follow an 'if' element within an 'Implies' element.");
							descent.push(Element.THEN);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta', 'if' or 'then' expected, '"+localName+"' found.");
					}
					break;
				
				case IF:
				case FORMULA:
					startFORMULA(localName);
					break;
				case EXISTS:
					switch (localName.charAt(0)){
						case 'd':
							if (lastSibling.peek() == Element.FORMULA) throw new SAXException("RIF-Core: 'declare' element must preceed the 'formula' element when within an 'Exists' element.");
							descent.push(Element.DECLARE);
							lastSibling.push(null);
							break;
						case 'f':
							if (lastSibling.peek() != Element.DECLARE) throw new SAXException("RIF-Core: 'formula' element, if it exists, must be preceeded by a 'declare' element when within an 'Exists' element.");
							descent.push(Element.FORMULA);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'declare' or 'formula' expected, '"+localName+"' found.");
					}
					break;
				case AND:
				case OR:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'f':
							descent.push(Element.FORMULA);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta' or 'formula' expected, '"+localName+"' found.");
					}
					break;
				case FRAME:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'o':
							if (lastSibling.peek() == Element.OBJECT) throw new SAXException("RIF-Core: 'object' element must be unique within a 'Frame' element.");
							if (lastSibling.peek() == Element.SLOT) throw new SAXException("RIF-Core: 'object' element must preceed any 'slot' element within a 'Frame' element.");
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
							throw new SAXException("RIF-Core: 'id', 'meta', 'object' or 'slot' expected, '"+localName+"' found.");
					}
					break;
				case EQUAL:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'l':
							if (lastSibling.peek() == Element.LEFT) throw new SAXException("RIF-Core: 'left' element must be unique within an 'Equal' element.");
							if (lastSibling.peek() == Element.RIGHT) throw new SAXException("RIF-Core: 'left' element must preceed any 'right' element within an 'Equal' element.");
							descent.push(Element.LEFT);
							lastSibling.push(null);
							break;
						case 'r':
							if (lastSibling.peek() != Element.LEFT) throw new SAXException("RIF-Core: 'right' element must be preceeded by a 'left' element within an 'Equal' element.");
							descent.push(Element.RIGHT);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta', 'left' or 'right' expected, '"+localName+"' found.");
					}
					break;
				case MEMBER:
					switch (localName.charAt(0)){
						case 'i':
							switch (localName.charAt(1)){
								case 'd':
									if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
									descent.push(Element.ID);
									lastSibling.push(null);
									break;
								case 'n':
									if (lastSibling.peek() == Element.INSTANCE) throw new SAXException("RIF-Core: 'instance' element must be unique within an 'Memeber' element.");
									if (lastSibling.peek() == Element.CLASS) throw new SAXException("RIF-Core: 'instance' element must preceed any 'class' element within an 'Member' element.");
									descent.push(Element.INSTANCE);
									lastSibling.push(null);
									break;
								default:
									throw new SAXException("RIF-Core: 'id', 'meta', 'instance' or 'class' expected, '"+localName+"' found.");
							}
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'c':
							if (lastSibling.peek() != Element.INSTANCE) throw new SAXException("RIF-Core: 'class' element must be preceeded by an 'instance' element within an 'Equal' element.");
							descent.push(Element.CLASS);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta', 'instance' or 'class' expected, '"+localName+"' found.");
					}
					break;
				case SLOT_FIRST:
					if (lastSibling.peek() == null){
						d = startTERM(localName,atts);
						if (d instanceof RIFDatum)
							currentFrame.setPredicate((RIFDatum) d);
						else
							throw new SAXException("RIF-Core: 'slot' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
					}else{
						descent.pop();
						lastSibling.pop();
						d = startTERM(localName,atts);
						if (d instanceof RIFDatum)
							currentFrame.setObject((RIFDatum) d);
						else
							throw new SAXException("RIF-Core: 'slot' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
					}
				case OBJECT:
					if (d == null) {
						d = startTERM(localName,atts);
						if (d instanceof RIFDatum)
							currentFrame.setSubject((RIFDatum) d);
						else
							throw new SAXException("RIF-Core: 'object' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
					}
				case RIGHT:
					if (d == null) {
						d = startTERM(localName,atts);
						if (d instanceof RIFDatum)
							currentEqual.setRight((RIFDatum) d);
						else
							throw new SAXException("RIF-Core: 'right' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
					}
				case LEFT:
					if (d == null) {
						d = startTERM(localName,atts);
						if (d instanceof RIFDatum)
							currentEqual.setLeft((RIFDatum) d);
						else
							throw new SAXException("RIF-Core: 'left	' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
					}
				case INSTANCE:
					if (d == null) {
						d = startTERM(localName,atts);
						if (d instanceof RIFDatum)
							currentMember.setInstance((RIFDatum) d);
						else
							throw new SAXException("RIF-Core: 'instance' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
					}
				case CLASS:
					if (d == null) {
						d = startTERM(localName,atts);
						if (d instanceof RIFDatum)
							currentMember.setInClass((RIFDatum) d);
						else
							throw new SAXException("RIF-Core: 'class' elements must contain only 'Var', 'Const' or 'External' elements, i.e. base terms.");
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
									if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
									descent.push(Element.ID);
									lastSibling.push(null);
									break;
								case 't':
									if (lastSibling.peek() == Element.ITEMS) throw new SAXException("RIF-Core: 'items' element, if it exists, must be unique in a 'List' element.");
									descent.push(Element.ITEMS);
									lastSibling.push(null);
									break;
								default:
									throw new SAXException("RIF-Core: 'id', 'meta' or 'items' expected, '"+localName+"' found.");
							}
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta' or 'items' expected, '"+localName+"' found.");
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
				case GROUND_EXTERNAL_EXPR:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'c':
							if (lastSibling.peek() == Element.GROUND_EXTERNAL_EXPR_CONTENT) throw new SAXException("RIF-Core: 'content' element must be the unique in an 'External' element.");
							descent.push(Element.GROUND_EXTERNAL_EXPR_CONTENT);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta' or 'content' expected, '"+localName+"' found.");
					}
					break;
				case GROUND_EXTERNAL_EXPR_CONTENT:
					switch (localName.charAt(0)){
						case 'E':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Expr' element must be the sole child of a 'content' element.");
							descent.push(Element.GROUND_EXPR);
							lastSibling.push(null);
							
							RIFExpr expr = new RIFExpr();
							try {
								((RIFExternalExpr)currentExternal.peek()).setExpr(expr);
							} catch (ClassCastException e){
								throw new SAXException("RIF-Core: 'External' as predicate must be a formula statement outside of an atomic construct.");
							}
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Expr' expected, '"+localName+"' found.");
					}
					break;
				case EXTERNAL_EXPR:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'c':
							if (lastSibling.peek() == Element.EXTERNAL_EXPR_CONTENT) throw new SAXException("RIF-Core: 'content' element must be the unique in an 'External' element.");
							descent.push(Element.EXTERNAL_EXPR_CONTENT);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta' or 'content' expected, '"+localName+"' found.");
					}
					break;
				case EXTERNAL_EXPR_CONTENT:
					switch (localName.charAt(0)){
						case 'E':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Expr' element must be the sole child of a 'content' element.");
							descent.push(Element.EXPR);
							lastSibling.push(null);
							
							RIFExpr expr = new RIFExpr();
							try {
								((RIFExternalExpr)currentExternal.peek()).setExpr(expr);
							} catch (ClassCastException e){
								throw new SAXException("RIF-Core: 'External' as predicate must be a formula statement outside of an atomic construct.");
							}
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Expr' expected, '"+localName+"' found.");
					}
					break;
				case GROUND_EXPR:
					currentAtom = new RIFAtom();
					try {
						((RIFExternalExpr)currentExternal.peek()).getExpr().setCommand(currentAtom, names.peek());
					} catch (ClassCastException e){
						throw new SAXException("RIF-Core: 'External' as predicate must be a formula statement outside of an atomic construct.");
					}
				case GROUND_ATOM:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'o':
							if (lastSibling.peek() == Element.OP) throw new SAXException("RIF-Core: 'op' element must be unique within an 'Expr' element.");
							if (lastSibling.peek() == Element.GROUND_ARGS) throw new SAXException("RIF-Core: 'op' element must preceed any 'args' element within an 'Expr' element.");
							descent.push(Element.OP);
							lastSibling.push(null);
							break;
						case 'a':
							if (lastSibling.peek() != Element.OP) throw new SAXException("RIF-Core: 'args' element must be preceeded by an 'op' element within an 'Expr' element.");
							descent.push(Element.GROUND_ARGS);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta', 'op' or 'args' expected, '"+localName+"' found.");
					}
					break;
				case EXPR:
					currentAtom = new RIFAtom();
					try {
						((RIFExternalExpr)currentExternal.peek()).getExpr().setCommand(currentAtom, names.peek());
					} catch (ClassCastException e){
						throw new SAXException("RIF-Core: 'External' as predicate must be a formula statement outside of an atomic construct.");
					}
				case ATOM:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'o':
							if (lastSibling.peek() == Element.OP) throw new SAXException("RIF-Core: 'op' element must be unique within an 'Expr' or 'Atom' element.");
							if (lastSibling.peek() == Element.ARGS) throw new SAXException("RIF-Core: 'op' element must preceed any 'args' element within an 'Expr' or 'Atom' element.");
							descent.push(Element.OP);
							lastSibling.push(null);
							break;
						case 'a':
							if (lastSibling.peek() != Element.OP) throw new SAXException("RIF-Core: 'args' element must be preceeded by an 'op' element within an 'Expr' or 'Atom' element.");
							descent.push(Element.ARGS);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta', 'op' or 'args' expected, '"+localName+"' found.");
					}
					break;
				case OP:
					switch (localName.charAt(0)){
						case 'C':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Const' element must be the sole child of an 'op' element.");
							descent.push(Element.CONST);
							lastSibling.push(null);
							
							currentAtom.setOp(startConst(atts));
							break;
						default:
							throw new SAXException("RIF-Core: 'Const' expected, '"+localName+"' found.");
					}
					break;
				case GROUND_ARGS:
					lastSibling.pop();
					lastSibling.push(null);
					d = startGROUNDTERM(localName,atts);
					if(d instanceof RIFDatum)
						currentAtom.addArg((RIFDatum) d);
					else if (d instanceof RIFList){
						currentList.push((RIFList) d);
					} else
						throw new SAXException("RIF-Core: 'arg' elements must contain only 'Const', 'List' and 'External' elements, i.e. base terms.");
					break;
				case ARGS:
					lastSibling.pop();
					lastSibling.push(null);
					d = startTERM(localName,atts);
					if(d instanceof RIFDatum)
						currentAtom.addArg((RIFDatum) d);
					else if (d instanceof RIFList){
						currentList.push((RIFList) d);
					} else
						throw new SAXException("RIF-Core: 'arg' elements must contain only 'Var', 'Const', 'List' and 'External' elements, i.e. base terms.");
					break;
				case FORMULA_EXTERNAL:
					switch (localName.charAt(0)){
						case 'i':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'id' element, if it exists, must be the first child of any element.");
							descent.push(Element.ID);
							lastSibling.push(null);
							break;
						case 'm':
							if (lastSibling.peek() != null && lastSibling.peek() != Element.ID) throw new SAXException("RIF-Core: 'meta' element, if it exists, must be the first child of its parent or follow an 'id' element.");
							descent.push(Element.META);
							lastSibling.push(null);
							break;
						case 'c':
							if (lastSibling.peek() == Element.FORMULA_CONTENT) throw new SAXException("RIF-Core: 'content' element must be the unique in an 'External' element.");
							descent.push(Element.FORMULA_CONTENT);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'id', 'meta' or 'content' expected, '"+localName+"' found.");
					}
					break;
				case FORMULA_CONTENT:
					switch (localName.charAt(0)){
						case 'A':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Atom' element must be the sole child of an 'content' element.");
							descent.push(Element.ATOM);
							lastSibling.push(null);
							
							currentAtom = new RIFAtom();
							try {
								((RIFExternalValue)currentExternal.peek()).setVal(currentAtom);
							} catch (ClassCastException e){
								throw new SAXException("RIF-Core: 'External' as data must be located inside of an atomic construct.");
							}
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Atom' expected, '"+localName+"' found.");
					}
					break;
					
				case THEN:
					switch (localName.charAt(0)){
						case 'A':
							switch (localName.charAt(1)){
								case 't':
									if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Atom' element, if it exists, must be the sole child of a 'then' element.");
									descent.push(Element.ATOM);
									lastSibling.push(null);
									
									currentAtom = new RIFAtom();
									currentRule.setHead(currentAtom);
									
									break;
								case 'n':
									if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'And' element, if it exists, must be the sole child of a 'then' element.");
									descent.push(Element.THEN_AND);
									lastSibling.push(null);
									
									currentFormula.push(new RIFAnd());
									currentRule.setHead(currentFormula.peek());
									
									break;
								default:
									throw new SAXException("RIF-Core: 'And', 'Frame' or 'Atom' expected, '"+localName+"' found.");
							}
							break;
						case 'F':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Frame' element, if it exists, must be the sole child of a 'then' element.");
							descent.push(Element.FRAME);
							lastSibling.push(null);
							
							currentFrame = new RIFFrame();
							currentRule.setHead(currentFrame);
							
							break;
						default:
							throw new SAXException("RIF-Core: 'And', 'Frame' or 'Atom' expected, '"+localName+"' found.");
					}
					break;
				case THEN_AND:
					switch (localName.charAt(0)){
						case 'f':
							descent.push(Element.THEN_AND_FORMULA);
							lastSibling.push(null);
							break;
						default:
							throw new SAXException("RIF-Core: 'formula' expected, '"+localName+"' found.");
					}
					break;
				case THEN_AND_FORMULA:
					switch (localName.charAt(0)){
						case 'A':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Atom' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
							descent.push(Element.ATOM);
							lastSibling.push(null);
							
							currentAtom = new RIFAtom();
							currentFormula.peek().addFormula(currentAtom);
							
							break;
						case 'F':
							if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Frame' element, if it exists, must be the sole child of a 'formula' element when within a 'Forall'.");
							descent.push(Element.FRAME);
							lastSibling.push(null);
							
							currentFrame = new RIFFrame();
							currentFormula.peek().addFormula(currentFrame);
							
							break;
						default:
							throw new SAXException("RIF-Core: 'Frame' or 'Atom' expected, '"+localName+"' found.");
					}
					break;
					
				default:
					throw new SAXException("RIF-Core: '"+descent.peek().toString()+"' element must not contain child elements.");
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
						if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'And' element, if it exists, must be the sole child of an element.");
						descent.push(Element.AND);
						lastSibling.push(null);
						
						RIFAnd an = new RIFAnd();
						pushToFormula(an);
						currentFormula.push(an);
						
						break;
					case 't':
						if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Atom' element, if it exists, must be the sole child of an element.");
						descent.push(Element.ATOM);
						lastSibling.push(null);
						
						currentAtom = new RIFAtom();
						pushToFormula(currentAtom);
						
						break;
					default:
						throw new SAXException("RIF-Core: 'And', 'Or', 'Exists', 'Equal', 'Member', 'Frame', 'Atom' or 'External' expected, '"+localName+"' found.");
				}
				break;
			case 'O':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Or' element, if it exists, must be the sole child of an element.");
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
								if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Exists' element, if it exists, must be the sole child of an element.");
								descent.push(Element.EXISTS);
								lastSibling.push(null);
								
								RIFExists exists = new RIFExists();
								pushToFormula(exists);
								currentFormula.push(exists);
								
								break;
							case 't':
								if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'External' element, if it exists, must be the sole child of an element.");
								descent.push(Element.FORMULA_EXTERNAL);
								lastSibling.push(null);
								
								RIFExternalValue ext = new RIFExternalValue();
								pushToFormula(ext);
								currentExternal.push(ext);
								
								break;
							default:
								throw new SAXException("RIF-Core: 'And', 'Or', 'Exists', 'Equal', 'Member', 'Frame', 'Atom' or 'External' expected, '"+localName+"' found.");
						}
						break;
					case 'q':
						if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Equal' element, if it exists, must be the sole child of an element.");
						descent.push(Element.EQUAL);
						lastSibling.push(null);
						
						currentEqual = new RIFEqual();
						pushToFormula(currentEqual);
						
						break;
					default:
						throw new SAXException("RIF-Core: 'And', 'Or', 'Exists', 'Equal', 'Member', 'Frame', 'Atom' or 'External' expected, '"+localName+"' found.");
				}
				break;
			case 'M':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Member' element, if it exists, must be the sole child of an element.");
				descent.push(Element.MEMBER);
				lastSibling.push(null);
				
				currentMember = new RIFMember();
				pushToFormula(currentMember);
				
				break;
			case 'F':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Frame' element, if it exists, must be the sole child of an element.");
				descent.push(Element.FRAME);
				lastSibling.push(null);
				
				currentFrame = new RIFFrame();
				pushToFormula(currentFrame);
				
				break;
			default:
				throw new SAXException("RIF-Core: 'And', 'Or', 'Exists', 'Equal', 'Member', 'Frame', 'Atom' or 'External' expected, '"+localName+"' found.");
		}
	}
	
	private RIFConst<?> startConst(Attributes atts) throws SAXException {
		if (atts.getValue("type") != null){
//System.out.println(atts.getValue("type"));
			URI uri = findURI(atts.getValue("type"));
			if (uri.toString().equals(RIFIRIConst.datatype)){
				currentConst = new RIFIRIConst();
			}else if (uri.toString().equals(RIFLocalConst.datatype)){
				currentConst = new RIFLocalConst();
			} else {
				currentConst = new RIFTypedConst(uri);
			}
		}else{
			currentConst = new RIFStringConst();
		}
		return currentConst;
	}
	
	private RIFData startTERM(String localName, Attributes atts) throws SAXException{
		switch (localName.charAt(0)){
			case 'C':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Const' element, if it exists, must be the sole child of an element.");
				descent.push(Element.CONST);
				lastSibling.push(null);
				
				return startConst(atts);
			case 'V':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Var' element, if it exists, must be the sole child of an element.");
				descent.push(Element.VAR);
				lastSibling.push(null);
				
				currentVar = new RIFVar();
				return currentVar;
			case 'L':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'List' element, if it exists, must be the sole child of an element.");
				descent.push(Element.LIST);
				lastSibling.push(null);
				
				return new RIFList();
			case 'E':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'External' element, if it exists, must be the sole child of an element.");
				descent.push(Element.EXTERNAL_EXPR);
				lastSibling.push(null);
				
				currentExternal.push(new RIFExternalExpr());
				return (RIFExternalExpr) currentExternal.peek();
			default:
				throw new SAXException("RIF-Core: 'Const', 'Var', 'List' or 'External' expected, '"+localName+"' found.");
		}
	}
	
	private RIFData startGROUNDTERM(String localName, Attributes atts) throws SAXException{
		switch (localName.charAt(0)){
			case 'C':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'Const' element, if it exists, must be the sole child of an element.");
				descent.push(Element.CONST);
				lastSibling.push(null);
				
				return startConst(atts);
			case 'L':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'List' element, if it exists, must be the sole child of an element.");
				descent.push(Element.LIST);
				lastSibling.push(null);

				return new RIFList();
			case 'E':
				if (lastSibling.peek() != null) throw new SAXException("RIF-Core: 'External' element, if it exists, must be the sole child of an element.");
				descent.push(Element.GROUND_EXTERNAL_EXPR);
				lastSibling.push(null);

				currentExternal.push(new RIFExternalExpr());
				return (RIFExternalExpr) currentExternal.peek();
			default:
				throw new SAXException("RIF-Core: 'Const', 'List' or 'External' expected, '"+localName+"' found.");
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
	
	@SuppressWarnings("unused")
	private void handleContent() throws SAXException {
		if (partialContent != null){
			String content = partialContent.toString();
			partialContent = null;
			
			if (!descent.isEmpty())
elementSwitch:	switch (descent.peek()){
					case CONST:
						if (currentConst instanceof RIFURIConst)
							((RIFURIConst) currentConst).setData(findURI(content));
						else
							((RIFTypedConst) currentConst).setData(content);
						break elementSwitch;
					case IRICONST:
						break elementSwitch;
						
					case LOCATION:
						currentLocation = findURI(content);
						break elementSwitch;
					case PROFILE:
						currentProfile = findURI(content);
						break elementSwitch;
					
					case VAR:
						// Roll back to outside 'Var' element.
						Element other = this.descent.pop();
						
						String scopedName = content;
						// If We are in a 'Forall' statement...
						if (currentForAll != null){
							// Find the most recently scoped version of the name.
							do {
								scopedName += "*";
							} while (names.peek().contains(scopedName));
							// If the given name has been checked...
nameSeek:					do {
								// [Choose next potentially valid altered variable name]
								scopedName = scopedName.substring(0, scopedName.length() - 1);
								// ... look in all parent formuli, ascending back up through the nesting order (confusingly viewed as 'descending' from tail to head of a queue in a linked list)...
								if (!currentFormula.isEmpty()){
									Iterator<RIFFormula> formIter = currentFormula.iterator();
existsSeek:							while (formIter.hasNext()){
										RIFFormula f = formIter.next();
										// ... If the formuli is in an 'exists'...
										if (f instanceof RIFExists){
											RIFExists e = (RIFExists) f;
											// ... check if the variable was declared in it...
											if (e.containsExistentialVar(scopedName)){
												// If the variable is within scope, assign the referenced variable the same rule index, then break: job done.
												currentVar.setName(scopedName, e.getExistentialVar(scopedName).getNode().getIndex());
												// Re-descend to actual depth
												this.descent.push(other);
												// exit switch
												break elementSwitch;
											// ... or if it is being declared in it.
											} else if (this.descent.peek() == Element.DECLARE){
												// If the Var is being declared, check if the name already exists in scope
												if (names.peek().contains(scopedName))
													// If the name already exists in scope, append an asterix.
													scopedName += "*";
												// ...then break from the name seeking loop.
												break nameSeek;
											}
										}
									}
								} else if (this.descent.peek() == Element.DECLARE){
									// If the Var is being declared, break from the name seeking loop.
									break nameSeek;
								}
								// If the variable has not been declared in an exists, but has been declared in the 'Forall'...
								if (currentForAll.containsVar(scopedName)){
									// ... assign the referenced variable the same rule index, then break: job done.
									currentVar.setName(scopedName, currentForAll.getUniversalVar(scopedName).getNode().getIndex());
									// Re-descend to actual depth
									this.descent.push(other);
									// exit switch
									break elementSwitch;
								}
							} while (!scopedName.equals(content));
							// If the 'Var' is new and not being stated in a 'declare' element, then we are not in an appropriate place to declare variables. PANIC.
							if (this.descent.peek() != Element.DECLARE)
								throw new SAXException("RIF-Core: Cannot use 'Var' element with contents '"+content+"' not 'declare'd in an ancestor 'Forall' or 'Exists' element.");
						}
						// If we are outside a 'Forall' or in an 'Declare' element of a 'Forall' or 'Exists', then we can add a new rule index and name,
						// either to the global set or the current scoped rule, as appropriate.
						currentVar.setName(scopedName,names.peek().contains(scopedName) ? names.peek().indexOf(scopedName) : names.peek().size());
//System.out.println("\t"+scopedName);
						if (!names.peek().contains(scopedName))
							names.peek().add(scopedName);
						
						// Re-descend to actual depth
						this.descent.push(other);
						// exit switch
						break elementSwitch;
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
				throw new SAXException("RIF-Core: Closing tag mismatch on '"+descent.peek().toString()+"' with '"+localName+"'.");
			
			switch (descent.peek()){
				case FOR_ALL:
					currentForAll = null;
					break;
				case ITEMS:
					currentList.pop();
					break;
				case FORMULA_EXTERNAL:
				case EXTERNAL_EXPR:
				case GROUND_EXTERNAL_EXPR:
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
			
			Element newLastSibling = descent.pop();
			lastSibling.pop();
			if (!lastSibling.isEmpty()){
				lastSibling.pop();
				lastSibling.push(newLastSibling);
			}
		}
	}
	
}