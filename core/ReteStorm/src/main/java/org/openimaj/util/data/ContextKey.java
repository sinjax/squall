package org.openimaj.util.data;

/**
 * @author David Monks <dm11g08@ecs.soton.ac.uk>
 *
 */
public enum ContextKey {
	/**
	 * The key to extract the last function to process the context.
	 */
	PREV_FUNC_KEY("lastFunction")
	,
	/**
	 * The key to extract the last stream to carry the context.
	 */
	STREAM_KEY("stream")
	,
	/**
	 * The key to extract the bindings from a context.
	 */
	BINDINGS_KEY("bindings")
	,
	/**
	 * The key to extract the triple from a context.
	 */
	TRIPLE_KEY("triple")
	,
	/**
	 * The key to extract the atom from a context.
	 */
	ATOM_KEY("atom")
	;
	
	private String key;
	
	private ContextKey(String key){
		this.key = key;
	}
	
	/**
	 * @return the string value that is the equivalent key in any context using values from this enum
	 */
	public String toString(){
		return this.key;
	}
	
}
