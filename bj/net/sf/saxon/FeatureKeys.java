package net.sf.saxon;


/**
  * FeatureKeys defines a set of constants, names of Saxon configuration
  * options which can be supplied to the Saxon implementations of the JAXP
  * interfaces TransformerFactory, SchemaFactory, Validator, and ValidationHandler.
  *
  * @author Michael H. Kay
  */


public abstract class FeatureKeys {

	/**
	 * ALLOW_EXTERNAL_FUNCTIONS must be a Boolean; it determines whether calls to external functions are allowed.
     * More specifically, it disallows all of the following:
     *
     * <ul>
     * <li>Calls to Java extension functions</li>
     * <li>Use of the XSLT system-property() function to access Java system properties</li>
     * <li>Use of a relative URI in the <code>xsl:result-document</code> instruction</li>
     * <li>Calls to XSLT extension instructions</li>
     * </ul>
     *
     * <p>Note that this option does not disable use of the <code>doc()</code> function or similar
     * functions to access the filestore of the machine where the transformation or query is running.
     * That should be done using a user-supplied <code>URIResolver</code></p>
	*/

	public static final String ALLOW_EXTERNAL_FUNCTIONS =
	        "http://saxon.sf.net/feature/allow-external-functions";

    /**
    * COLLATION_URI_RESOLVER must be a {@link net.sf.saxon.sort.CollationURIResolver}.
     * This resolver will be used to resolve collation URIs used in stylesheets compiled or executed under the
     * control of this TransformerFactory
    */

    public static final String COLLATION_URI_RESOLVER =
            "http://saxon.sf.net/feature/collation-uri-resolver";

    /**
    * COLLECTION_URI_RESOLVER must be a {@link net.sf.saxon.CollectionURIResolver}.
     * This resolver will be used to resolve collection URIs used in calls of the collection() function
    */

    public static final String COLLECTION_URI_RESOLVER =
            "http://saxon.sf.net/feature/collection-uri-resolver";

    /**
     * COMPILE_WITH_TRACING must be a Boolean. If true, stylesheets and queries
     * are compiled with tracing enabled, but the choice of a trace listener
     * is deferred until run time (see {@link Controller#addTraceListener(net.sf.saxon.trace.TraceListener)})
     */

    public static final String COMPILE_WITH_TRACING =
            "http://saxon.sf.net/feature/compile-with-tracing";

    /**
     * CONFIGURATION must be an instance of {@link Configuration}. This attribute cannot be set on the
     * Configuration itself, but it can be set on various JAXP factory objects such as a
     * TransformerFactory or DocumentBuilderFactory, to ensure that several such factories use the same
     * configuration. Note that other configuration options are held in the Configuration object, so setting
     * this attribute will cancel all others that have been set. Also, if two factories share the same
     * configuration, then setting an attribute on one affects all the others.
     */

    public static final String CONFIGURATION =
            "http://saxon.sf.net/feature/configuration";

    /**
    * DTD_VALIDATION must be a Boolean. This determines whether source documents should be
    * parsed with DTD-validation enabled.
    */

    public static final String DTD_VALIDATION =
            "http://saxon.sf.net/feature/validation";

    /**
     * EXPAND_ATTRIBUTE_DEFAULTS must be a Boolean; it determines whether fixed and default values defined
     * in a schema or DTD will be expanded. By default (and for conformance with the
     * specification) validation against a DTD or schema will cause default values defined in the schema
     * or DTD to be inserted into the document. Setting this feature to false suppresses this behaviour. In
     * the case of DTD-defined defaults this only works if the XML parser reports whether each attribute was
     * specified in the source or generated by expanding a default value. Not all XML parsers report this
     * information.
    */

    public static final String EXPAND_ATTRIBUTE_DEFAULTS =
            "http://saxon.sf.net/feature/expandAttributeDefaults";

    /**
     * LINE_NUMBERING must be a Boolean; it determines whether line numbers are maintained for the
     * source document
    */

    public static final String LINE_NUMBERING =
            "http://saxon.sf.net/feature/linenumbering";

    /**
    * MESSAGE_EMITTER_CLASS must be the class name of a class that implements net.sf.saxon.event.Receiver.
    * Despite the name, it is not required to be an instance of net.sf.saxon.event.Emitter
    */

    public static final String MESSAGE_EMITTER_CLASS =
            "http://saxon.sf.net/feature/messageEmitterClass";

    /**
     * MODULE_URI_RESOLVER must be an instance of {@link net.sf.saxon.query.ModuleURIResolver}. This is a user-written
     * class that takes responsibility for locating XQuery modules.
    */

    public static final String MODULE_URI_RESOLVER =
            "http://saxon.sf.net/feature/moduleURIResolver";    

    /**
    * NAME_POOL must be an instance of net.sf.saxon.om.NamePool
    */

    public static final String NAME_POOL =
            "http://saxon.sf.net/feature/namePool";

    /**
     * OUTPUT_URI_RESOLVER must be an instance of {@link net.sf.saxon.OutputURIResolver}. This is a
     * user-written class that takes responsibility for disposing of the result trees produced
     * using XSLT xsl:result-document instruction
    */

    public static final String OUTPUT_URI_RESOLVER =
            "http://saxon.sf.net/feature/outputURIResolver";

    /**
     * PRE_EVALUATE_DOC_FUNCTION is a boolean. If set, calls to the doc() or document() function with a statically-known
     * document URI are evaluated at compile time, so that the document only needs to be parsed and constructed once.
     * The effect of this is that subsequent changes to the contents of the file will not be reflected during
     * query or stylesheet processing
     */

    public static final String PRE_EVALUATE_DOC_FUNCTION =
            "http://saxon.sf.net/feature/preEvaluateDocFunction";

    /**
	* RECOGNIZE_URI_QUERY_PARAMETERS must be a Boolean; it determines whether query parameters (things after a question mark)
     * in a URI passed to the document() or doc() function are specially recognized by the system default URIResolver.
     * Allowed parameters include, for example validation=strict to perform schema validation, and strip-space=yes
     * to perform stripping of all whitespace-only text nodes.
	*/

	public static final String RECOGNIZE_URI_QUERY_PARAMETERS =
	        "http://saxon.sf.net/feature/recognize-uri-query-parameters";

    /**
    * RECOVERY_POLICY must be an Integer: one of {@link Configuration#RECOVER_SILENTLY},
    * {@link Configuration#RECOVER_WITH_WARNINGS}, or {@link Configuration#DO_NOT_RECOVER}
    */

    public static final String RECOVERY_POLICY =
            "http://saxon.sf.net/feature/recoveryPolicy";

    /**
     * SCHEMA_URI_RESOLVER must be an instance of {@link net.sf.saxon.type.SchemaURIResolver}. This is a user-written
     * class that takes responsibility for locating schema documents.
    */

    public static final String SCHEMA_URI_RESOLVER =
            "http://saxon.sf.net/feature/schemaURIResolver";

    /**
    * SCHEMA_VALIDATION must be an Integer. This determines whether source documents should be
    * parsed with schema-validation enabled. The string takes one of the values
     * {@link net.sf.saxon.om.Validation#STRICT}, {@link net.sf.saxon.om.Validation#LAX},
     * {@link net.sf.saxon.om.Validation#PRESERVE}, {@link net.sf.saxon.om.Validation#SKIP}.
    */

    public static final String SCHEMA_VALIDATION =
            "http://saxon.sf.net/feature/schema-validation";

    /**
     * SOURCE_PARSER_CLASS must be the full class name of an XMLReader. This identifies the parser
     * used for source documents.
     */

    public static final String SOURCE_PARSER_CLASS =
            "http://saxon.sf.net/feature/sourceParserClass";

    /**
     * STRIP_WHITESPACE must be a string set to one of the values "all", "none", or "ignorable".
     * This determines what whitespace is stripped during tree construction: "all" removes all
     * whitespace-only text nodes; "ignorable" removes whitespace text nodes in element-only content
     * (as identified by a DTD or Schema), and "none" preserves all whitespace. This whitespace stripping
     * is additional to any stripping caused by the xsl:strip-space declaration in a stylesheet.
     */

    public static final String STRIP_WHITESPACE =
            "http://saxon.sf.net/feature/strip-whitespace";

    /**
     * STYLE_PARSER_CLASS must be an XMLReader. This identifies the parser used for stylesheets and
     * schema modules.
     */

    public static final String STYLE_PARSER_CLASS =
            "http://saxon.sf.net/feature/styleParserClass";

    /**
     * TIMING must be an Boolean; it determines whether basic timing information is output to System.err
     * (This attribute is a bit of a misnomer; it outputs timing information when used from the command line,
     * but also basic tracing information when used from the Java API: for example, names of output files
     * written using xsl:result-document, and names of classes dynamically loaded)
    */

    public static final String TIMING =
            "http://saxon.sf.net/feature/timing";

	/**
	* TRACE_EXTERNAL_FUNCTIONS must be a Boolean; it determines whether the loading and binding of extension
     * functions is traced
	*/

	public static final String TRACE_EXTERNAL_FUNCTIONS =
	        "http://saxon.sf.net/feature/trace-external-functions";

	/**
	* TRACE_OPTIMIZER_DECISIONS must be a Boolean; it determines whether decisions made by the optimizer
     * are traced
	*/

	public static final String TRACE_OPTIMIZER_DECISIONS =
	        "http://saxon.sf.net/feature/trace-optimizer-decisions";

    /**
    * TRACE_LISTENER must be an instance of a class that implements 
     * {@link net.sf.saxon.trace.TraceListener}. Setting this property automatically
     * sets {@link #COMPILE_WITH_TRACING} to true.
    */

    public static final String TRACE_LISTENER =
            "http://saxon.sf.net/feature/traceListener";
    

	/**
	* TREE_MODEL must be an Integer: {@link net.sf.saxon.event.Builder#LINKED_TREE}
     *  or {@link net.sf.saxon.event.Builder#TINY_TREE}
	*/

	public static final String TREE_MODEL =
	        "http://saxon.sf.net/feature/treeModel";

    /**
     * USE_PI_DISABLE_OUTPUT_ESCAPING must be a Boolean. This determines a TransformerHandler created
     * with this Factory or Configuration recognizes the processing instructions
     * <code>Result.PI_DISABLE_OUTPUT_ESCAPING</code> and <code>Result.PI_ENABLE_OUTPUT_ESCAPING</code>
     * in the input stream as instructions to disable or to re-enable output escaping.\
    */

    public static final String USE_PI_DISABLE_OUTPUT_ESCAPING =
            "http://saxon.sf.net/feature/use-pi-disable-output-escaping";


    /**
    * VALIDATION_WARNINGS must be a Boolean. This determines whether validation errors in result
    * documents should be treated as fatal. By default they are fatal; with this option set, they
    * are treated as warnings.
    */

    public static final String VALIDATION_WARNINGS =
            "http://saxon.sf.net/feature/validation-warnings";

    /**
    * VERSION_WARNING must be a Boolean. This determines whether a warning should be output when
     * running an XSLT 2.0 processor against an XSLT 1.0 stylesheet. The XSLT specification requires
     * this to be done by default.
    */

    public static final String VERSION_WARNING =
            "http://saxon.sf.net/feature/version-warning";

    /**
     * XINCLUDE must be a Boolean. This determines whether XInclude processing should be performed by default
     * when XML source documents (including stylesheets and schemas) are read.
    */

    public static final String XINCLUDE =
            "http://saxon.sf.net/feature/xinclude-aware";

    /**
     * XML_VERSION is a character string. This determines the XML version used by the Configuration: the
     * value must be "1.0" or "1.1". For details, see {@link Configuration#setXMLVersion(int)}.
     *
     * <p>Note that source documents specifying xml version="1.0" or "1.1" are accepted
     * regardless of this setting. The effect of this switch is to change the validation
     * rules for types such as Name and NCName, to change the meaning of \i and \c in
     * regular expressions, and to determine whether the serializer allows XML 1.1 documents
     * to be constructed. </p>
     */

    public static final String XML_VERSION =
            "http://saxon.sf.net/feature/xml-version";

    private FeatureKeys() {
    }
}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Contributor(s):
//
