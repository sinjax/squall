package org.openimaj.rif;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.openimaj.rif.contentHandler.RIFEntailmentImportProfiles;
import org.openimaj.rif.contentHandler.RIFOWLImportProfiles;
import org.openimaj.rif.rules.RIFGroup;
import org.openimaj.rif.rules.RIFSentence;
import org.xml.sax.SAXException;

/**
 * Converts a set of rules expressed in RIF/XML to a set of rules in a different format.
 * @author David Monks <david.monks@zepler.net>
 */
public class RIFRuleSet implements Iterable<RIFGroup> {

	/**
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println("%s %s".replaceFirst("%s", "Hello"));
		try {
			RIFRuleSet rs = new RIFOWLImportProfiles().parse(
								new URI("http://www.w3.org/2005/rules/test/repository/tc/IRI_from_RDF_Literal/IRI_from_RDF_Literal-premise.rif"),
								new URI("http://www.w3.org/ns/entailment/Core")
							);
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
	
	//  VARIABLES
	
	private URI base;
	private Map<String,URI> prefixes;
	private Map<URI,URI> imports;
	private List<RIFGroup> rootGroup;
	private RIFEntailmentImportProfiles parserMap;
	private Stack<URI> profile;
	
	//  CONSTRUCTORS
	
	/**
	 * @param profile 
	 * @param pm 
	 * 
	 */
	public RIFRuleSet(URI profile,
					  RIFEntailmentImportProfiles pm){
		super();
		
		this.profile = new Stack<URI>();
		this.prefixes = new HashMap<String, URI>();
		this.imports = new HashMap<URI, URI>();
		this.rootGroup = new ArrayList<RIFGroup>();
		
		this.profile.push(profile);
		this.parserMap = pm;
	}
	
	/**
	 * @param profile 
	 * @param pm 
	 * @param base
	 * @param prefixes
	 */
	public RIFRuleSet(URI profile,
			  		  RIFEntailmentImportProfiles pm,
			  		  URI base,
					  Map<String,URI> prefixes){
		this(profile, pm);
		
		this.base = base;
		this.prefixes = prefixes;
	}
	
	/**
	 * @param profile 
	 * @param pm 
	 * @param imports
	 */
	public RIFRuleSet(URI profile,
			  		  RIFEntailmentImportProfiles pm,
			  		  Map<URI,URI> imports){
		this(profile, pm);
		
		this.imports = imports;
	}
	
	/**
	 * @param profile 
	 * @param pm 
	 * @param base
	 * @param prefixes
	 * @param imports
	 */
	public RIFRuleSet(URI profile,
			  		  RIFEntailmentImportProfiles pm,
			  		  URI base,
					  Map<String,URI> prefixes,
					  Map<URI,URI> imports){
		this(profile, pm, base, prefixes);
				
		this.imports = imports;
	}
	
	/**
	 * @param profile 
	 * @param pm 
	 * @param base
	 * @param prefixes
	 * @param imports
	 * @param root
	 */
	public RIFRuleSet(URI profile,
			  		  RIFEntailmentImportProfiles pm,
			  		  URI base,
					  Map<String,URI> prefixes,
					  Map<URI,URI> imports,
					  RIFGroup root){
		this(profile, pm, base, prefixes, imports);
		
		this.rootGroup.add(root);
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
	public void addImport(URI loc, URI prof) {
		if (this.parserMap == null){
			this.imports.put(loc, prof);
			return;
		}
		if (prof == null){
			try {
				this.parserMap.parse(loc, this.profile.peek(), this);
			} catch (Exception e) {
				this.imports.put(loc, prof);
			}
			return;
		}
		this.profile.push(prof);
		try {
			this.parserMap.parse(loc, prof, this);
		} catch (Exception e) {
//			e.printStackTrace();
			this.imports.put(loc, prof);
		}
		this.profile.pop();
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
	 * @param root
	 */
	public void addRootGroup(RIFGroup root){
		this.rootGroup.add(root);
	}
	
	/**
	 * @return
	 */
	public RIFGroup getRootGroup(){
		return this.rootGroup.get(this.rootGroup.size() - 1);
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
		for (RIFSentence sentence : rootGroup){
			fgroups += "\n"+sentence.toString();
		}
		return "[ \n  "+fbase+"\n  "+fprefixes+"\n  "+fimports+"\n  "+fgroups+"\n]";
	}

	@Override
	public Iterator<RIFGroup> iterator() {
		return this.rootGroup.iterator();
	}
	
	//  RIFXMLContentHandler Class
	
		
	
}