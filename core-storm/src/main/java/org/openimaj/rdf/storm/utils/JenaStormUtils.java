/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.rdf.storm.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.util.data.Context;

import backtype.storm.Config;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * A collections to tools for letting Jena play nicely with Storm
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author David Monks (dm11g08@ecs.soton.ac.uk)
 * 
 */
public class JenaStormUtils {

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_URI extends Serializer<Node_URI> {

		@Override
		public void write(Kryo kryo, Output output, Node_URI object) {
			output.writeString(object.getURI());
		}

		@Override
		public Node_URI read(Kryo kryo, Input input, Class<Node_URI> type) {
			return (Node_URI) NodeFactory.createURI(input.readString());
		}

	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class TemplateSerialiser extends Serializer<Template> {

		@Override
		public void write(Kryo kryo, Output output, Template object) {
			final BasicPattern bgp = object.getBGP();
			output.writeInt(bgp.size());
			for (final Triple triple : bgp) {
				kryo.writeClassAndObject(output, triple);
			}
		}

		@Override
		public Template read(Kryo kryo, Input input, Class<Template> type) {
			final BasicPattern bgp = new BasicPattern();
			final int count = input.readInt();
			for (int i = 0; i < count; i++) {
				bgp.add((Triple) kryo.readClassAndObject(input));
			}
			return new Template(bgp);
		}

	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_Literal extends Serializer<Node_Literal> {

		@Override
		public void write(Kryo kryo, Output output, Node_Literal object) {
			final LiteralLabel label = object.getLiteral();
			output.writeString(label.getLexicalForm());
			output.writeString(label.language());
			output.writeString(label.getDatatypeURI());
		}

		@Override
		public Node_Literal read(Kryo kryo, Input input, Class<Node_Literal> type) {
			final String lexicalForm = input.readString();
			final String langauge = input.readString();
			final String datatypeURI = input.readString();
			final RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(datatypeURI);
			return (Node_Literal) NodeFactory.createLiteral(lexicalForm, langauge, dtype);

		}

	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_Blank extends Serializer<Node_Blank> {

		@Override
		public void write(Kryo kryo, Output output, Node_Blank object) {
			final String blankNodeString = object.toString();
			output.writeString(blankNodeString);
		}

		@Override
		public Node_Blank read(Kryo kryo, Input input, Class<Node_Blank> type) {
			final String label = input.readString();
			final Node_Blank retNode = (Node_Blank) NodeFactory.createAnon(AnonId.create(label));
			return retNode;
		}

	}

	/**
	 * @author David Monks<dm11g08@ecs.soton.ac.uk>
	 * 
	 */
	public static class NodeSerialiser_Variable extends Serializer<Node_Variable> {

		@Override
		public void write(Kryo kryo, Output output, Node_Variable object) {
			final String blankNodeString = object.toString();
			output.writeString(blankNodeString);
		}

		@Override
		public Node_Variable read(Kryo kryo, Input input, Class<Node_Variable> type) {
			final String label = input.readString();
			final Node_Variable retNode = (Node_Variable) NodeFactory.createVariable(label.replaceFirst("\\?", ""));
			return retNode;
		}

	}
	
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_RuleVariable extends Serializer<Node_RuleVariable> {

		@Override
		public void write(Kryo kryo, Output output, Node_RuleVariable object) {
			final String blankNodeString = object.toString();
			output.writeString(blankNodeString);
			output.writeInt(object.getIndex());
			
		}

		@Override
		public Node_RuleVariable read(Kryo kryo, Input input, Class<Node_RuleVariable> type) {
			String label = input.readString();
//			label = label.replaceFirst("\\?", "");
			int index = input.readInt();
			Node_RuleVariable rv = new Node_RuleVariable(label, index);
			return rv;
		}

	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class TripleSerialiser extends Serializer<Triple> {

		@Override
		public void write(Kryo kryo, Output output, Triple object) {
			final Node s = object.getSubject();
			final Node p = object.getPredicate();
			final Node o = object.getObject();
			kryo.writeClassAndObject(output, s);
			kryo.writeClassAndObject(output, p);
			kryo.writeClassAndObject(output, o);
		}

		@Override
		public Triple read(Kryo kryo, Input input, Class<Triple> type) {
			final Node s = (Node) kryo.readClassAndObject(input);
			final Node p = (Node) kryo.readClassAndObject(input);
			final Node o = (Node) kryo.readClassAndObject(input);
			return new Triple(s, p, o);
		}

	}
	
	/**
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 * 
	 */
	public static class FunctorSerialiser extends Serializer<Functor> {

		@Override
		public void write(Kryo kryo, Output output, Functor object) {
			final String name = object.getName();
			final Node[] args = object.getArgs();
			kryo.writeClassAndObject(output, name);
			kryo.writeClassAndObject(output, args);
		}

		@Override
		public Functor read(Kryo kryo, Input input, Class<Functor> type) {
			final String s = (String) kryo.readClassAndObject(input);
			final Node[] args = (Node[]) kryo.readClassAndObject(input);
			return new Functor(s, args);
		}

	}
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class TriplePatternSerialiser extends Serializer<TriplePattern> {

		@Override
		public void write(Kryo kryo, Output output, TriplePattern object) {
			final Node s = object.getSubject();
			final Node p = object.getPredicate();
			final Node o = object.getObject();
			kryo.writeClassAndObject(output, s);
			kryo.writeClassAndObject(output, p);
			kryo.writeClassAndObject(output, o);
		}

		@Override
		public TriplePattern read(Kryo kryo, Input input, Class<TriplePattern> type) {
			final Node s = (Node) kryo.readClassAndObject(input);
			final Node p = (Node) kryo.readClassAndObject(input);
			final Node o = (Node) kryo.readClassAndObject(input);
			return new TriplePattern(s, p, o);
		}

	}

	/**
	 * 
	 * @author David Monks <dm11g08@ecs.soton.ac.uk>
	 */
	public static class GraphSerialiser extends Serializer<Graph> {

		@Override
		public void write(Kryo kryo, Output output, Graph object) {
			output.writeInt(object.size());
			final Iterator<Triple> it = object.find(null, null, null);
			while (it.hasNext()) {
				final Triple next = it.next();
				kryo.writeClassAndObject(output, next);
			}
		}

		@Override
		public Graph read(Kryo kryo, Input input, Class<Graph> type) {
			final int size = input.readInt();
			Graph graph = null;
			graph = new GraphMem();
			final List<Triple> overflow = new ArrayList<Triple>();
			for (int i = 0; i < size; i++) {
				final Object obj = kryo.readClassAndObject(input);
				try {
					graph.add((Triple) obj);
				} catch (final AddDeniedException ex) {
					overflow.add((Triple) obj);
				}
			}
			Iterator<Triple> it = overflow.iterator();
			while (!overflow.isEmpty()) {
				if (!it.hasNext())
					it = overflow.iterator();
				try {
					graph.add(it.next());
					it.remove();
				} catch (final AddDeniedException ex) {
				}
			}
			return graph;
		}

	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_ARRAY extends Serializer<Node[]> {

		@Override
		public void write(Kryo kryo, Output output, Node[] object) {
			output.writeInt(object.length);
			for (final Node node : object) {
				kryo.writeClassAndObject(output, node);
			}
		}

		@Override
		public Node[] read(Kryo kryo, Input input, Class<Node[]> type) {
			final Node[] out = new Node[input.readInt()];
			for (int i = 0; i < out.length; i++) {
				out[i] = (Node) kryo.readClassAndObject(input);
			}
			return out;
		}

	}
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class NodeSerialiser_RuleVariableARRAY extends Serializer<Node_RuleVariable[]> {

		@Override
		public void write(Kryo kryo, Output output, Node_RuleVariable[] object) {
			output.writeInt(object.length);
			for (final Node_RuleVariable node : object) {
				kryo.writeClassAndObject(output, node);
			}
		}

		@Override
		public Node_RuleVariable[] read(Kryo kryo, Input input, Class<Node_RuleVariable[]> type) {
			final Node_RuleVariable[] out = new Node_RuleVariable[input.readInt()];
			for (int i = 0; i < out.length; i++) {
				out[i] = (Node_RuleVariable) kryo.readClassAndObject(input);
			}
			return out;
		}

	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class KestrelServerSpec_Serializer extends Serializer<KestrelServerSpec> {

		@Override
		public void write(Kryo kryo, Output output, KestrelServerSpec object) {
			output.writeString(object.host);
			output.writeInt(object.port);
		}

		@Override
		public KestrelServerSpec read(Kryo kryo, Input input, Class<KestrelServerSpec> type) {
			return new KestrelServerSpec(input.readString(), input.readInt());
		}

	}

	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class RuleSerializer extends Serializer<Rule> {

		@Override
		public void write(Kryo kryo, Output output, Rule object) {
			output.writeString(object.toString());
		}

		@Override
		public Rule read(Kryo kryo, Input input, Class<Rule> type) {
			return Rule.parseRule(input.readString());
		}

	}
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class URISerializer extends Serializer<URI> {

		@Override
		public void write(Kryo kryo, Output output, URI object) {
			output.writeString(object.toString());
		}

		@Override
		public URI read(Kryo kryo, Input input, Class<URI> type) {
			try {
				return new URI(input.readString());
			} catch (URISyntaxException e) {
				throw new UnsupportedOperationException();
			}
		}

	}
	
	/**
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class ContextSerializer extends Serializer<Context> {

		@Override
		public void write(Kryo kryo, Output output, Context object) {
			output.writeInt(object.size());
			for (String key : object.keySet()) {
				output.writeString(key);
				kryo.writeClassAndObject(output, object.get(key));
			}
		}

		@Override
		public Context read(Kryo kryo, Input input, Class<Context> type) {
			int count = input.readInt();
			Context ret = new Context();
			for (int i = 0; i < count; i++) {
				String key = input.readString();
				Object value = kryo.readClassAndObject(input);
				ret.put(key, value);
			}
			return ret;
		}

	}

	/**
	 * @param conf
	 *            register some Jena serialisers to this configuration
	 */
	public static void registerSerializers(Config conf) {
		conf.registerSerialization(Context.class, ContextSerializer.class);
		conf.registerSerialization(URI.class, URISerializer.class);
		conf.registerSerialization(Node_URI.class, NodeSerialiser_URI.class);
		conf.registerSerialization(Node_Literal.class, NodeSerialiser_Literal.class);
		conf.registerSerialization(Node_Blank.class, NodeSerialiser_Blank.class);
		conf.registerSerialization(Node_RuleVariable.class, NodeSerialiser_RuleVariable.class);
		conf.registerSerialization(Node_Variable.class, NodeSerialiser_Variable.class);
		conf.registerSerialization(TriplePattern.class, TriplePatternSerialiser.class);
		conf.registerSerialization(Triple.class, TripleSerialiser.class);
		conf.registerSerialization(Functor.class, FunctorSerialiser.class);
		conf.registerSerialization(ArrayList.class);
		conf.registerSerialization(KestrelServerSpec.class, KestrelServerSpec_Serializer.class);
		conf.registerSerialization(Rule.class, RuleSerializer.class);
		conf.registerSerialization(Graph.class, GraphSerialiser.class);
		conf.registerSerialization(GraphMem.class, GraphSerialiser.class);
		conf.registerSerialization(MultiUnion.class, GraphSerialiser.class);
		conf.registerSerialization(Template.class, TemplateSerialiser.class);
		conf.registerSerialization(ElementFilter.class);
		// conf.registerSerialization(Node_NULL.class);
		// conf.registerSerialization(Node_Blank.class);
		conf.registerSerialization(Node_RuleVariable[].class, NodeSerialiser_RuleVariableARRAY.class);
		conf.registerSerialization(Node[].class, NodeSerialiser_ARRAY.class);
	}

	/**
	 * @param conf
	 */
	public static void registerSerializers(Kryo conf) {
		conf.addDefaultSerializer(Context.class, ContextSerializer.class);
		conf.addDefaultSerializer(URI.class, URISerializer.class);
		conf.addDefaultSerializer(Node_URI.class, NodeSerialiser_URI.class);
		conf.addDefaultSerializer(Node_Literal.class, NodeSerialiser_Literal.class);
		conf.addDefaultSerializer(Node_Blank.class, NodeSerialiser_Blank.class);
		conf.addDefaultSerializer(Node_RuleVariable.class, NodeSerialiser_RuleVariable.class);
		conf.addDefaultSerializer(Node_Variable.class, NodeSerialiser_Variable.class);
		conf.addDefaultSerializer(TriplePattern.class, TriplePatternSerialiser.class);
		conf.addDefaultSerializer(Triple.class, TripleSerialiser.class);
		conf.addDefaultSerializer(Functor.class, FunctorSerialiser.class);
		conf.register(ArrayList.class);
		conf.addDefaultSerializer(KestrelServerSpec.class, KestrelServerSpec_Serializer.class);
		conf.addDefaultSerializer(Rule.class, RuleSerializer.class);
		conf.addDefaultSerializer(Graph.class, GraphSerialiser.class);
		conf.addDefaultSerializer(GraphMem.class, GraphSerialiser.class);
		conf.addDefaultSerializer(MultiUnion.class, GraphSerialiser.class);
		conf.addDefaultSerializer(Template.class, TemplateSerialiser.class);
		conf.register(ElementFilter.class);
		conf.addDefaultSerializer(Node_RuleVariable[].class, NodeSerialiser_RuleVariableARRAY.class);
		conf.addDefaultSerializer(Node[].class, NodeSerialiser_ARRAY.class);
	}
	
	/**
	 * @return
	 */
	public static Kryo kryo(){
		Kryo kryo = new Kryo();
		registerSerializers(kryo);
		return kryo;
	}
}
