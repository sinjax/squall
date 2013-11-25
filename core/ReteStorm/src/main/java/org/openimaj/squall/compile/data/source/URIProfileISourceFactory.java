package org.openimaj.squall.compile.data.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.openimaj.rif.imports.schemes.HTTPSchemeFunction;
import org.openimaj.rif.imports.schemes.JavaSchemeFunction;
import org.openimaj.rif.imports.schemes.URLSchemeFunction;
import org.openimaj.squall.data.ISource;
import org.openimaj.squall.functions.rif.sources.NTriplesISourceFactory;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class URIProfileISourceFactory {
	
	private final class LocationProfileISource implements ISource<Stream<Context>> {
		private Function<URI, InputStream> reader;
		private Function<InputStream, Stream<Context>> creator;
		private URI location;
		private InputStream is;

		public LocationProfileISource(URI location, Function<URI, InputStream> reader, Function<InputStream, Stream<Context>> creator) {
			this.reader = reader;
			this.creator = creator;
			this.location = location;
		}

		@Override
		public Stream<Context> apply(Stream<Context> in) {
			return apply();
		}

		@Override
		public Stream<Context> apply() {
			return this.creator.apply(is);
		}

		@Override
		public void setup() {
			this.is = reader.apply(location);
		}

		@Override
		public void cleanup() {
			try {
				this.is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private final class LocationISource implements ISource<Stream<Context>> {
		private Function<URI, Stream<Context>> reader;
		private URI location;

		public LocationISource(URI location, Function<URI, Stream<Context>> r) {
			this.reader = r;
		}

		@Override
		public Stream<Context> apply(Stream<Context> in) {
			return apply();
		}

		@Override
		public Stream<Context> apply() {
			return this.reader.apply(location);
		}

		@Override
		public void setup() {
		}

		@Override
		public void cleanup() {
		}
	}

	

	private static HashMap<String, Function<URI, Stream<Context>>> pureSchemeFunctions 
		= new HashMap<String, Function<URI,Stream<Context>>>();
	private static HashMap<String, Function<URI, InputStream>> schemeFunctions 
		= new HashMap<String, Function<URI,InputStream>>();
	private static HashMap<URI, Function<InputStream, Stream<Context>>> profileFunctions 
		= new HashMap<URI, Function<InputStream,Stream<Context>>>();
	private static URIProfileISourceFactory instance;
	
	/**
	 * 
	 */
	private URIProfileISourceFactory() {
	}
	
	static{
		URI NTRIPLES_URI;
		URI TURTLE_URI;
		try {
			NTRIPLES_URI = new URI("http://www.w3.org/ns/stream/NTriples");
			TURTLE_URI = new URI("http://www.w3.org/ns/stream/Turtle");
		} catch (URISyntaxException e) { throw new RuntimeException(e);}
		// Register the default scheme functions
		schemeFunctions.put("http", new HTTPSchemeFunction());
		schemeFunctions.put("file", new URLSchemeFunction());
		schemeFunctions.put("java", new JavaSchemeFunction());
		
		// Register the default pure scheme functions
		// TODO: maybe kestrel? 
		
		// Register the default profile functions
		profileFunctions.put(NTRIPLES_URI, new NTriplesProfileFunction());
		profileFunctions.put(TURTLE_URI, new TurtleProfileFunction());
	}
	
	

	/**
	 * Given a location and a profile produce a source for a {@link Stream} of {@link Context}
	 * instances which contain triples.
	 * 
	 * The {@link URI#getScheme()} of the location is used against registered scheme factories.
	 * The default scheme factories are the File and Http scheme factories. Novel scheme
	 * factories can be added using the {{@link #registerScheme(String, Function)}}. A Scheme
	 * is a {@link Function} which turns a {@link URI} to a {@link InputStream}.
	 * 
	 * If a scheme is found, this {@link InputStream} is consumed by a function defined by the profile URI. 
	 * The default profiles supported are the {@link NTriplesISourceFactory} which assumes the
	 * stream provided is of NTriples
	 * 
	 * If no such scheme is found, an attempt is made to find a pure scheme handler (registered using
	 * {@link #registerPureScheme(String,Function)}. These schemes can consume a given URI without
	 * a profile, i.e. each location produces a {@link Stream}.
	 * 
	 * If neither are found a {@link RuntimeException} is thrown.
	 * 
	 * @param location
	 * @param profile
	 * @return An object which can create a stream of triples
	 */
	public ISource<Stream<Context>> createSource(URI location, URI profile){
		Function<URI,InputStream> reader = schemeFunctions .get(location.getScheme());
		if(reader == null){
			Function<URI,Stream<Context>> pureReader = pureSchemeFunctions.get(location.getScheme());
			if(pureReader == null){
				throw new RuntimeException(String.format("No handler was found for the URI %s with scheme %s",location,location.getScheme()));
			}
			return new LocationISource(location, pureReader);
		}
		Function<InputStream,Stream<Context>> creator = profileFunctions.get(profile);
		if(creator == null){
			throw new RuntimeException(String.format("No handler was found for the profile URI %s",profile));
		}
		return new LocationProfileISource(location, reader,creator);
	}
	
	/**
	 * register a new input stream creating stream
	 * @param scheme
	 * @param schemeFunction
	 */
	public static void registerScheme(String scheme, Function<URI, InputStream> schemeFunction){
		schemeFunctions.put(scheme,schemeFunction);
		
	}
	
	/**
	 * register a new input stream creating stream
	 * @param scheme
	 * @param psf
	 */
	public static void registerPureScheme(String scheme, Function<URI,Stream<Context>> psf){
		pureSchemeFunctions.put(scheme, psf);
	}
	
	/**
	 * Register a new profile function
	 * @param profile
	 * @param profileFunc
	 */
	public static void registerProfile(URI profile, Function<InputStream, Stream<Context>> profileFunc){
		profileFunctions.put(profile, profileFunc);
	}
	
	/**
	 * @return the factory instance
	 */
	public static URIProfileISourceFactory instance(){
		if(instance == null){
			instance = new URIProfileISourceFactory();
		}
		return instance;
	}

}
