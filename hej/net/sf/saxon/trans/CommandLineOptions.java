package net.sf.saxon.trans;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Builder;
import net.sf.saxon.functions.Component;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.Initializer;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.DayTimeDurationValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.UntypedAtomicValue;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;

/**
 * This is a helper class for classes such as net.sf.saxon.Transform and net.sf.saxon.Query that process
 * command line options
 */
public class CommandLineOptions {

    public static final int TYPE_BOOLEAN = 1;
    public static final int TYPE_FILENAME = 2;
    public static final int TYPE_CLASSNAME = 3;
    public static final int TYPE_ENUMERATION = 4;
    public static final int TYPE_INTEGER = 5;
    public static final int TYPE_QNAME = 6;
    public static final int TYPE_FILENAME_LIST = 7;
    public static final int TYPE_DATETIME = 8;
    public static final int TYPE_STRING = 9;
    public static final int TYPE_INTEGER_PAIR = 10;

    public static final int VALUE_REQUIRED = 1<<8;
    public static final int VALUE_PROHIBITED = 2<<8;

    /*@NotNull*/ HashMap<String, Integer> recognizedOptions = new HashMap<String, Integer>();
    /*@NotNull*/ HashMap<String, String> optionHelp = new HashMap<String, String>();
    /*@NotNull*/ Properties namedOptions = new Properties();
    /*@NotNull*/ Properties configOptions = new Properties();
    /*@NotNull*/ Map<String, Set<String>> permittedValues = new HashMap<String, Set<String>>();
    /*@NotNull*/ Map<String, String> defaultValues = new HashMap<String, String>();
    /*@NotNull*/ List<String> positionalOptions = new ArrayList();
    /*@NotNull*/ Properties paramValues = new Properties();
    /*@NotNull*/ Properties paramExpressions = new Properties();
    /*@NotNull*/ Properties paramFiles = new Properties();
    /*@NotNull*/ Properties serializationParams = new Properties();
    Initializer initializer;

    /**
     * Set the permitted options.
     * @param option A permitted option.
     */

    public void addRecognizedOption(String option, int optionProperties, String helpText) {
        recognizedOptions.put(option, optionProperties);
        optionHelp.put(option, helpText);
        if ((optionProperties & 0xff) == TYPE_BOOLEAN) {
            setPermittedValues(option, new String[]{"on", "off"}, "on");
        }
    }

    /**
     * Set the permitted values for an option
     * @param option the option keyword
     * @param values the set of permitted values
     * @param defaultValue the default value if the option is supplied but no value is given. May be null if no
     * default is defined.
     */

    public void setPermittedValues(String option, String[] values, /*@Nullable*/ String defaultValue) {
        Set<String> valueSet = new HashSet<String>();
        valueSet.addAll(Arrays.asList(values));
        permittedValues.put(option, valueSet);
        if (defaultValue != null) {
            defaultValues.put(option, defaultValue);
        }
    }

    /**
     * Display a list of the values permitted for an option with type enumeration
     * @param permittedValues the set of permitted values
     * @return the set of values as a string, pipe-separated
     */

    private static String displayPermittedValues(/*@NotNull*/ Set<String> permittedValues) {
        FastStringBuffer sb = new FastStringBuffer(20);
        for (String val: permittedValues) {
            if ("".equals(val)) {
                sb.append("\"\"");
            } else {
                sb.append(val);
            }
            sb.append('|');
        }
        sb.setLength(sb.length()-1);
        return sb.toString();
    }

    /**
     * Set the options actually present on the command line
     * @param args the options supplied on the command line
     * @throws XPathException if an unrecognized or invalid option is found
     */

    public void setActualOptions(/*@NotNull*/ String[] args) throws XPathException {
        for (String arg : args) {
            if ("-".equals(arg)) {
                positionalOptions.add(args[1]);
            } else if (arg.charAt(0) == '-') {
                String option;
                String value = "";
                if (arg.length() > 5 && arg.charAt(1) == '-') {
                    // --featureKey:value format
                    int colon = arg.lastIndexOf(':');
                    if (colon > 0 && colon < arg.length() - 1) {
                        option = arg.substring(2, colon);
                        value = arg.substring(colon + 1);
                        configOptions.setProperty(option, value);
                    } else if (colon > 0 && colon == arg.length() - 1) {
                        option = arg.substring(2, colon);
                        configOptions.setProperty(option, "");
                    } else {
                        option = arg.substring(2);
                        configOptions.setProperty(option, "true");
                    }
                } else {
                    int colon = arg.indexOf(':');
                    if (colon > 0 && colon < arg.length() - 1) {
                        option = arg.substring(1, colon);
                        value = arg.substring(colon + 1);
                    } else {
                        option = arg.substring(1);
                    }
                    if (recognizedOptions.get(option) == null) {
                        throw new XPathException("Command line option -" + option +
                                " is not recognized. Options available: " + displayPermittedOptions());
                    }
                    if (namedOptions.getProperty(option) != null) {
                        throw new XPathException("Command line option -" + option + " appears more than once");
                    } else if ("?".equals(value)) {
                        displayOptionHelp(option);
                        throw new XPathException("No processing requested");
                    } else {
                        if ("".equals(value)) {
                            int prop = recognizedOptions.get(option);
                            if ((prop & VALUE_REQUIRED) != 0) {
                                String msg = "Command line option -" + option + " requires a value";
                                if (permittedValues.get(option) != null) {
                                    msg += ": permitted values are " + displayPermittedValues(permittedValues.get(option));
                                }
                                throw new XPathException(msg);
                            }
                            String defaultValue = defaultValues.get(option);
                            if (defaultValue != null) {
                                value = defaultValue;
                            }
                        } else {
                            int prop = recognizedOptions.get(option);
                            if ((prop & VALUE_PROHIBITED) != 0) {
                                String msg = "Command line option -" + option + " does not expect a value";
                                throw new XPathException(msg);
                            }
                        }
                        Set<String> permitted = permittedValues.get(option);
                        if (permitted != null && !permitted.contains(value)) {
                            throw new XPathException("Bad option value " + arg +
                                    ": permitted values are " + displayPermittedValues(permitted));
                        }
                        namedOptions.setProperty(option, value);
                    }
                }
            } else {
                // handle keyword=value options
                int eq = arg.indexOf('=');
                if (eq >= 1) {
                    String keyword = arg.substring(0, eq);
                    String value = "";
                    if (eq < arg.length() - 1) {
                        value = arg.substring(eq + 1);
                    }
                    char ch = arg.charAt(0);
                    if (ch == '!' && eq >= 2) {
                        serializationParams.setProperty(keyword.substring(1), value);
                    } else if (ch == '?' && eq >= 2) {
                        paramExpressions.setProperty(keyword.substring(1), value);
                    } else if (ch == '+' && eq >= 2) {
                        paramFiles.setProperty(keyword.substring(1), value);
                    } else {
                        paramValues.setProperty(keyword, value);
                    }
                } else {
                    positionalOptions.add(arg);
                }
            }
        }
    }

    /**
     * Test whether there is any keyword=value option present
     * @return true if there are any keyword=value options
     */

    public boolean definesParameterValues() {
        return !serializationParams.isEmpty() ||
                !paramExpressions.isEmpty() ||
                !paramFiles.isEmpty() ||
                !paramValues.isEmpty();
    }

    /**
     * Prescan the command line arguments to see if any of them imply use of a schema-aware processor
     * @return true if a schema-aware processor is needed
     */

    public boolean testIfSchemaAware() {
        return getOptionValue("sa") != null ||
                    getOptionValue("outval") != null ||
                    getOptionValue("val") != null ||
                    getOptionValue("vlax") != null ||
                    getOptionValue("xsd") != null ||
                    getOptionValue("xsdversion") != null;
    }

    /**
     * Apply options to the Configuration
     * @param config the Configuration
     * @throws javax.xml.transform.TransformerException if invalid options are present
     */

    public void applyToConfiguration(/*@NotNull*/ final Configuration config) throws TransformerException {

        for (Enumeration e = configOptions.propertyNames(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            String value = configOptions.getProperty(name);
            try {
                config.setConfigurationProperty("http://saxon.sf.net/feature/" + name, value);
            } catch (IllegalArgumentException err) {
                throw new XPathException(err.getMessage());
            }
        }

        String value = getOptionValue("catalog");
        if (value != null) {
            if (getOptionValue("r") != null) {
                throw new XPathException("Cannot use -catalog and -r together");
            }
            if (getOptionValue("x") != null) {
                throw new XPathException("Cannot use -catalog and -x together");
            }
            if (getOptionValue("y") != null) {
                throw new XPathException("Cannot use -catalog and -y together");
            }
            try {
                config.getClass("org.apache.xml.resolver.CatalogManager", false, null);
                XmlCatalogResolver.setCatalog(value, config, getOptionValue("t") != null);
            } catch (XPathException err) {
                throw new XPathException("Failed to load Apache catalog resolver library", err);
            }
        }

        value = getOptionValue("cr");
        if (value != null) {
            Object resolver = config.getInstance(value, null);
            config.setConfigurationProperty(FeatureKeys.COLLECTION_URI_RESOLVER, resolver);
        }

        value = getOptionValue("dtd");
        if (value != null) {
            if ("on".equals(value)) {
                config.getParseOptions().setDTDValidationMode(Validation.STRICT);
            } else if ("off".equals(value)) {
                config.getParseOptions().setDTDValidationMode(Validation.SKIP);
            } else if ("recover".equals(value)) {
                config.getParseOptions().setDTDValidationMode(Validation.LAX);
            }
        }

        value = getOptionValue("expand");
        if (value != null) {
            config.getParseOptions().setExpandAttributeDefaults("on".equals(value));
        }

        value = getOptionValue("ext");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS,
                    "on".equals(value));
        }

        value = getOptionValue("l");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.LINE_NUMBERING,
                    "on".equals(value));
        }

        value = getOptionValue("m");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.MESSAGE_EMITTER_CLASS, value);
        }

        value = getOptionValue("opt");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.OPTIMIZATION_LEVEL, value);
        }

        value = getOptionValue("or");
        if (value != null) {
            Object resolver = config.getInstance(value, null);
            config.setConfigurationProperty(FeatureKeys.OUTPUT_URI_RESOLVER, resolver);
        }

        value = getOptionValue("outval");
        if (value != null) {
            Boolean isRecover = "recover".equals(value);
            config.setConfigurationProperty(FeatureKeys.VALIDATION_WARNINGS, isRecover);
            config.setConfigurationProperty(FeatureKeys.VALIDATION_COMMENTS, isRecover);
        }

        value = getOptionValue("r");
        if (value != null) {
            config.setURIResolver(config.makeURIResolver(value));
        }

        value = getOptionValue("strip");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.STRIP_WHITESPACE, value);
        }

        value = getOptionValue("TJ");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.TRACE_EXTERNAL_FUNCTIONS,
                    "on".equals(value));
        }

        value = getOptionValue("tree");
        if (value != null) {
            if ("linked".equals(value)) {
                config.setTreeModel(Builder.LINKED_TREE);
            } else if ("tiny".equals(value)) {
                config.setTreeModel(Builder.TINY_TREE);
            } else if ("tinyc".equals(value)) {
                config.setTreeModel(Builder.TINY_TREE_CONDENSED);
            }
        }

        value = getOptionValue("val");
        if (value != null) {
            if ("strict".equals(value)) {
                config.setConfigurationProperty(FeatureKeys.SCHEMA_VALIDATION, Validation.STRICT);
            } else if ("lax".equals(value)) {
                config.setConfigurationProperty(FeatureKeys.SCHEMA_VALIDATION, Validation.LAX);
            }
        }

        value = getOptionValue("versionmsg");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.VERSION_WARNING,
                    "on".equals(value));
        }

        value = getOptionValue("warnings");
        if (value != null) {
            if ("silent".equals(value)) {
                config.setConfigurationProperty(FeatureKeys.RECOVERY_POLICY,
                        Configuration.RECOVER_SILENTLY);
            } else if ("recover".equals(value)) {
                config.setConfigurationProperty(FeatureKeys.RECOVERY_POLICY,
                        Configuration.RECOVER_WITH_WARNINGS);
            } else if ("fatal".equals(value)) {
                config.setConfigurationProperty(FeatureKeys.RECOVERY_POLICY,
                        Configuration.DO_NOT_RECOVER);
            }
        }

        value = getOptionValue("x");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.SOURCE_PARSER_CLASS, value);
        }

        value = getOptionValue("xi");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.XINCLUDE,
                    "on".equals(value));
        }

        value = getOptionValue("xmlversion");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.XML_VERSION, value);
        }

        value = getOptionValue("xsdversion");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.XSD_VERSION, value);
        }

        value = getOptionValue("xsiloc");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.USE_XSI_SCHEMA_LOCATION,
                    "on".equals(value));
        }

        value = getOptionValue("xsltversion");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.XSLT_VERSION, value);
        }

        value = getOptionValue("y");
        if (value != null) {
            config.setConfigurationProperty(FeatureKeys.STYLE_PARSER_CLASS, value);
        }

        // The init option must be done last

        value = getOptionValue("init");
        if (value != null) {
            Initializer initializer = (Initializer)config.getInstance(value, null);
            initializer.initialize(config);
        }

    }

    /**
     * Display the list the permitted options
     * @return the list of permitted options, as a string
     */

    public String displayPermittedOptions() {
        String[] options = new String[recognizedOptions.size()];
        options = new ArrayList<String>(recognizedOptions.keySet()).toArray(options);
        Arrays.sort(options, Collator.getInstance());
        FastStringBuffer sb = new FastStringBuffer(100);
        for (String opt : options) {
            sb.append(" -");
            sb.append(opt);
        }
        return sb.toString();
    }

    /**
     * Display help for a specific option on the System.err output (in response to -opt:?)
     * @param option: the option for which help is required
     */

    private void displayOptionHelp(String option) {
        System.err.println("Help for -" + option + " option");
        int prop = recognizedOptions.get(option);
        if ((prop & VALUE_PROHIBITED) == 0) {
            switch ((prop & 0xff)) {
                case TYPE_BOOLEAN:
                    System.err.println("Value: on|off");
                    break;
                case TYPE_INTEGER:
                    System.err.println("Value: integer");
                    break;
                case TYPE_FILENAME:
                    System.err.println("Value: file name");
                    break;
                case TYPE_FILENAME_LIST:
                    System.err.println("Value: list of file names, semicolon-separated");
                    break;
                case TYPE_CLASSNAME:
                    System.err.println("Value: Java fully-qualified class name");
                    break;
                case TYPE_QNAME:
                    System.err.println("Value: QName in Clark notation ({uri}local)");
                    break;
                case TYPE_STRING:
                    System.err.println("Value: string");
                    break;
                case TYPE_INTEGER_PAIR:
                    System.err.println("Value: int,int");
                    break;
                case TYPE_ENUMERATION:
                    String message = "Value: one of ";
                    message += displayPermittedValues(permittedValues.get(option));
                    System.err.println(message);
                    break;
                default:
                    break;
            }
        }
        System.err.println("Meaning: " + optionHelp.get(option));
    }

    /**
     * Get the value of a named option. Returns null if the option was not present on the command line.
     * Returns "" if the option was present but with no value ("-x" or "-x:").
     * @param option the option keyword
     * @return the option value, or null if not specified.
     */

    public String getOptionValue(String option) {
        return namedOptions.getProperty(option);
    }

    /**
     * Get the options specified positionally, that is, without a leading "-"
     * @return the list of positional options
     */

    /*@NotNull*/ public List<String> getPositionalOptions() {
        return positionalOptions;
    }

    /**
     * Apply requested parameters to a controller, a query context, or a set of output properties, as appropriate
     * @param config the Saxon configuration
     * @param controller  The controller to be used for a transformation. May be null.
     * @param qcontext  The dynamic query context. May be null.
     * @param outputProperties the serialization properties. May be null.
     * @throws javax.xml.transform.TransformerException if invalid options are found
     */

    public void setParams(/*@NotNull*/ Configuration config, /*@Nullable*/ Controller controller, /*@Nullable*/ DynamicQueryContext qcontext, /*@Nullable*/ Properties outputProperties)
            throws TransformerException {
        boolean useURLs = "on".equals(getOptionValue("u"));
        for (Enumeration e = paramValues.propertyNames(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            String value = paramValues.getProperty(name);
            if (controller != null) {
                controller.setParameter(name, new UntypedAtomicValue(value));
            }
            if (qcontext != null) {
                qcontext.setParameterValue(name, new UntypedAtomicValue(value));
            }
        }
        for (Enumeration e = paramFiles.propertyNames(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            String value = paramFiles.getProperty(name);
            Object sources = loadDocuments(value, useURLs, config, true);
            if (controller != null) {
                controller.setParameter(name, sources);
            }
            if (qcontext != null) {
                qcontext.setParameter(name, sources);
            }
        }
        for (Enumeration e = paramExpressions.propertyNames(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            String value = paramExpressions.getProperty(name);
            // parameters starting with "?" are taken as XPath expressions
            XPathEvaluator xpe = new XPathEvaluator(config);
            XPathExpression expr = xpe.createExpression(value);
            XPathDynamicContext context = expr.createDynamicContext();
            ValueRepresentation val = SequenceExtent.makeSequenceExtent(expr.iterate(context));
            if (controller != null) {
                controller.setParameter(name, val);
            }
            if (qcontext != null) {
                qcontext.setParameterValue(name, val);
            }
        }
        for (Enumeration e = serializationParams.propertyNames(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            String value = serializationParams.getProperty(name);
            // parameters starting with "!" are taken as output properties
            // Allow the prefix "!saxon:" instead of "!{http://saxon.sf.net}"
            if (name.startsWith("saxon:")) {
                name = "{" + NamespaceConstant.SAXON + "}" + name.substring(6);
            }
            if (controller != null) {
                controller.setOutputProperty(name, value);
            }
            if (outputProperties != null) {
                outputProperties.setProperty(name, value);
            }
        }
    }


    /**
     * Load a document, or all the documents in a directory, given a filename or URL
     * @param sourceFileName the name of the source file or directory
     * @param useURLs true if the filename argument is to be treated as a URI
     * @param config the Saxon configuration
     * @param useSAXSource true if the method should use a SAXSource rather than a StreamSource
     * @return if sourceFileName represents a single source document, return a Source object representing
     *         that document. If sourceFileName represents a directory, return a List containing multiple Source
     *         objects, one for each file in the directory.
     * @throws javax.xml.transform.TransformerException if access to documents fails
     */

    /*@Nullable*/ public static Object loadDocuments(/*@NotNull*/ String sourceFileName, boolean useURLs,
                                       /*@NotNull*/ Configuration config, boolean useSAXSource)
            throws TransformerException {

        Source sourceInput;
        XMLReader parser;
        if (useURLs || sourceFileName.startsWith("http:") || sourceFileName.startsWith("file:")) {
            sourceInput = config.getURIResolver().resolve(sourceFileName, null);
            if (sourceInput == null) {
                sourceInput = config.getSystemURIResolver().resolve(sourceFileName, null);
            }
            return sourceInput;
        } else if (sourceFileName.equals("-")) {
            // take input from stdin
            if (useSAXSource) {
                parser = config.getSourceParser();
                sourceInput = new SAXSource(parser, new InputSource(System.in));
            } else {
                sourceInput = new StreamSource(System.in);
            }
            return sourceInput;
        } else {
            File sourceFile = new File(sourceFileName);
            if (!sourceFile.exists()) {
                throw new TransformerException("Source file " + sourceFile + " does not exist");
            }
            if (sourceFile.isDirectory()) {
                parser = config.getSourceParser();
                List<Source> result = new ArrayList<Source>(20);
                String[] files = sourceFile.list();
                for (String file1 : files) {
                    File file = new File(sourceFile, file1);
                    if (!file.isDirectory()) {
                        if (useSAXSource) {
                            InputSource eis = new InputSource(file.toURI().toString());
                            sourceInput = new SAXSource(parser, eis);
                            // it's safe to use the same parser for each document, as they
                            // will be processed one at a time.
                        } else {
                            sourceInput = new StreamSource(file.toURI().toString());
                        }
                        result.add(sourceInput);
                    }
                }
                return result;
            } else {
                if (useSAXSource) {
                    InputSource eis = new InputSource(sourceFile.toURI().toString());
                    sourceInput = new SAXSource(config.getSourceParser(), eis);
                } else {
                    sourceInput = new StreamSource(sourceFile.toURI().toString());
                }
                return sourceInput;
            }
        }
    }

    public static void loadAdditionalSchemas(/*@NotNull*/ Configuration config, String additionalSchemas)
            throws TransformerException {
        StringTokenizer st = new StringTokenizer(additionalSchemas, ";");
        while (st.hasMoreTokens()) {
            String schema = st.nextToken();
            File schemaFile = new File(schema);
            if (!schemaFile.exists()) {
                throw new TransformerException("Schema document " + schema + " not found");
            }
            config.addSchemaSource(new StreamSource(schemaFile));
        }
    }

    /*@NotNull*/ public static String showExecutionTime(long millisecs) {
        if (millisecs < 1000) {
            return millisecs + "ms";
        } else {
            try {
                DayTimeDurationValue d = new DayTimeDurationValue(1, 0, 0, 0, millisecs/1000, ((int)millisecs%1000)*1000);
                long days = ((NumericValue)d.getComponent(Component.DAY)).longValue();
                long hours = ((NumericValue)d.getComponent(Component.HOURS)).longValue();
                long minutes = ((NumericValue)d.getComponent(Component.MINUTES)).longValue();
                BigDecimal seconds = ((NumericValue)d.getComponent(Component.SECONDS)).getDecimalValue();
                FastStringBuffer fsb = new FastStringBuffer(256);
                if (days > 0) {
                    fsb.append(days + "days ");
                }
                if (hours > 0) {
                    fsb.append(hours + "h ");
                }
                if (minutes > 0) {
                    fsb.append(minutes + "m ");
                }
                fsb.append(seconds + "s");
                return fsb.toString() + " (" + millisecs + "ms)";
            } catch (XPathException e) {
                return millisecs + "ms";
            }

        }
    }

    /*@NotNull*/ public static String showExecutionTimeNano(long nanosecs) {
        if (nanosecs < 1e9) {
            return (nanosecs/1e6) + "ms";
        } else {
            try {
                DayTimeDurationValue d = new DayTimeDurationValue(1, 0, 0, 0, nanosecs/1000000000L, (int)(nanosecs%1000));
                long days = ((NumericValue)d.getComponent(Component.DAY)).longValue();
                long hours = ((NumericValue)d.getComponent(Component.HOURS)).longValue();
                long minutes = ((NumericValue)d.getComponent(Component.MINUTES)).longValue();
                BigDecimal seconds = ((NumericValue)d.getComponent(Component.SECONDS)).getDecimalValue();
                FastStringBuffer fsb = new FastStringBuffer(256);
                if (days > 0) {
                    fsb.append(days + "days ");
                }
                if (hours > 0) {
                    fsb.append(hours + "h ");
                }
                if (minutes > 0) {
                    fsb.append(minutes + "m ");
                }
                fsb.append(seconds + "s");
                return fsb.toString() + " (" + nanosecs/1e6 + "ms)";
            } catch (XPathException e) {
                return nanosecs/1e6 + "ms";
            }

        }
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
// The Initial Developer of the Original Code is Saxonica Limited.
// Portions created by ___ are Copyright (C) ___. All rights reserved.
//
// Contributor(s):
//