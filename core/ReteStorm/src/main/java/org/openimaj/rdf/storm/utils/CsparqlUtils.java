package org.openimaj.rdf.storm.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.debug.ParseTreeBuilder;
import org.antlr.runtime.tree.ParseTree;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import eu.larkc.csparql.parser.CSparqlLexer;
import eu.larkc.csparql.parser.CSparqlParser;
import eu.larkc.csparql.parser.PostProcessingException;
import eu.larkc.csparql.parser.SparqlProducer1_0;
import eu.larkc.csparql.parser.StreamInfo;
import eu.larkc.csparql.parser.TreeBox;
import eu.larkc.csparql.streams.formats.CSparqlQuery;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class CsparqlUtils {
	/**
	 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei
	 *         (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class CSparqlComponentHolder {
		/**
		 * @param jenaQuery
		 * @param streams
		 */
		public CSparqlComponentHolder(Query jenaQuery, Set<StreamInfo> streams) {
			this.simpleQuery = jenaQuery;
			this.streams = streams;
		}

		/**
		 * the normal SPARQL part of the query
		 */
		public Query simpleQuery;
		/**
		 * the FROM STREAM parts of the query
		 */
		public Set<StreamInfo> streams;

	}

	/**
	 * Construct a dummy engine + translator. Construct the query.
	 * 
	 * @param query
	 * @return a {@link CSparqlQuery} instance
	 * @throws IOException
	 */
	public static CSparqlComponentHolder parse(String query) throws IOException {
		try {
			TreeBox tb = parseInternal(query);
			SparqlProducer1_0 sp = new SparqlProducer1_0();
			Set<StreamInfo> streams = tb.getStreams();
			Query jenaQuery = QueryFactory.create(sp.produceSparql(tb));
			System.out.println(jenaQuery);
			System.out.println(tb);
			return new CSparqlComponentHolder(jenaQuery, streams);
		} catch (RecognitionException e) {
			throw new IOException(e);
		} catch (PostProcessingException e) {
			throw new IOException(e);
		}
	}

	private static TreeBox parseInternal(String query) throws RecognitionException, PostProcessingException
	{
		query = preprocessQuery(query);

		CharStream input = new ANTLRStringStream(query);
		CSparqlLexer lexer = new CSparqlLexer(input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		ParseTreeBuilder builder = new ParseTreeBuilder("queryWithReg");

		CSparqlParser parser = new CSparqlParser(tokens, builder);

		parser.queryWithReg();

		ParseTree debug = builder.getTree();

		TreeBox tb = TreeBox.create(debug);
		TreeBox.decorate(tb);

		return tb;

	}

	private static String preprocessQuery(String query)
	{
		Pattern compiledRegex1 = Pattern.compile(
				"\\\\u([\\da-fA-F])([\\da-fA-F])([\\da-fA-F])([\\da-fA-F])", 8);

		Pattern compiledRegex2 = Pattern
				.compile(
						"\\\\U([\\da-fA-F])([\\da-fA-F])([\\da-fA-F])([\\da-fA-F])([\\da-fA-F])([\\da-fA-F])([\\da-fA-F])([\\da-fA-F])",
						8);

		String unicode = new String();
		StringBuffer queryOk = new StringBuffer();

		Matcher regexMatcher = compiledRegex1.matcher(query);
		while (regexMatcher.find())
		{
			unicode = "0x" + regexMatcher.group(0).substring(2);
			try {
				char[] c = Character.toChars(Integer.decode(unicode).intValue());
				for (char literal : c) {
					unicode = "" + literal;
				}
				regexMatcher.appendReplacement(queryOk, unicode);
			} catch (NumberFormatException nfe) {
				nfe.toString();
			}
		}
		regexMatcher.appendTail(queryOk);

		query = queryOk.toString();

		queryOk = new StringBuffer();
		regexMatcher = compiledRegex2.matcher(query);
		while (regexMatcher.find())
		{
			unicode = "0x" + regexMatcher.group(0).substring(2);
			char[] c = Character.toChars(Integer.decode(unicode).intValue());
			for (char literal : c) {
				unicode = "" + literal;
			}
			regexMatcher.appendReplacement(queryOk, unicode);
		}
		regexMatcher.appendTail(queryOk);

		return queryOk.toString();
	}

	/**
	 * Fill in a template
	 * 
	 * @param query
	 * @param templateReplace
	 * @return template removed replaced with replace
	 */
	public static String templaceQuery(String query, Map<String, String> templateReplace) {
		String fixedString = query;
		for (Entry<String, String> replace : templateReplace.entrySet()) {
			fixedString = fixedString.replace(String.format("{%s}", replace.getKey()), replace.getValue());
		}
		return fixedString;
	}

	/**
	 * Replace all test variables with a single variable, good for cleaning up
	 * queries
	 * 
	 * @param queryStr
	 * @param with
	 *            replace with this
	 * @param clean
	 *            find all these
	 * @return cleaned
	 */
	public static String templaceQuery(String query, String with, String... clean) {
		String fixedString = query;
		for (String replace : clean) {
			fixedString = fixedString.replaceAll(String.format("[{]%s[}]", replace), with);
		}
		return fixedString;
	}
}
