package org.openimaj.rdf.storm.tool.lang;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * The various supported rule sets
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum RuleLanguageMode implements CmdLineOptionsProvider {
	/**
	 * Jena Rule Language parser.
	 */
	JENA {
		/**
		 * @return {@link JenaRuleLanguageHandler}
		 */
		@Override
		public RuleLanguageHandler getOptions() {
			return new JenaRuleLanguageHandler();
		}
	}
	,
	/**
	 * CSPARQL parser
	 */
	SPARQL {
		/**
		 * @return {@link SPARQLRuleLanguageHandler}
		 */
		@Override
		public RuleLanguageHandler getOptions() {
			return new SPARQLRuleLanguageHandler();
		}

	}
	,
	/**
	 * RIF parser
	 */
	RIF {
		/**
		 * @return {@link RIFRuleLanguageHandler}
		 */
		@Override
		public RuleLanguageHandler getOptions() {
			return new RIFRuleLanguageHandler();
		}

	}
	,
	/**
	 * RDF/XML parser connected to a Jena Model and provided with SPARQL Construct Queries representing reasoning rules to bake in.
	 */
	ONTOLOGY {
		/**
		 * @return {@link OntologyRuleLanguageHandler}
		 */
		@Override
		public RuleLanguageHandler getOptions() {
			return new OntologyRuleLanguageHandler();
		}

	}
	,
	/**
	 * CSPARQL parser which performs no work
	 */
	IDENTITY_SPARQL {
		/**
		 * @return {@link IdentitySPARQLRuleLanguageHandler}
		 */
		@Override
		public RuleLanguageHandler getOptions() {
			return new IdentitySPARQLRuleLanguageHandler();
		}

	}
	;

	@Override
	public abstract RuleLanguageHandler getOptions();

}
