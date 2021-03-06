package net.sf.saxon.functions;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.type.*;
import net.sf.saxon.value.*;

import java.util.HashMap;

/**
* This class contains static data tables defining the properties of standard functions. "Standard functions"
* here means the XPath 2.0 functions, the XSLT 2.0 functions, and a few selected extension functions
* which need special recognition.
*/

public abstract class StandardFunction {

    public static Value EMPTY = EmptySequence.getInstance();

    /**
     * Categories of functions, bit significant
     */

    public static final int CORE = 1;
    public static final int XSLT = 2;
    public static final int USE_WHEN = 4;
    public static final int XQUPDATE = 8;
    public static final int XPATH30 = 16;
    public static final int INTERNAL = 32;
    public static final int XSLT30 = 64;

    /**
     * This class is never instantiated
     */

    private StandardFunction() {
    }

    /**
     * Register a system function in the table of function details.
     * @param name the function name
     * @param implementationClass the class used to implement the function
     * @param opcode identifies the function when a single class implements several functions
     * @param minArguments the minimum number of arguments required
     * @param maxArguments the maximum number of arguments allowed
     * @param itemType the item type of the result of the function
     * @param cardinality the cardinality of the result of the function
     * @param applicability the host languages (and versions thereof) in which this function is available
     * @return the entry describing the function. The entry is incomplete, it does not yet contain information
     * about the function arguments.
    */

    /*@NotNull*/ public static Entry register(String name,
                                  Class implementationClass,
                                  int opcode,
                                  int minArguments,
                                  int maxArguments,
                                  ItemType itemType,
                                  int cardinality,
                                  int applicability) {
        Entry e = makeEntry(name, implementationClass, opcode, minArguments, maxArguments,
                itemType, cardinality, applicability);
        functionTable.put(name, e);
        return e;
    }

    /**
     * Make a table entry describing the signature of a function, with a reference to the implementation class.
     * @param name the function name
     * @param implementationClass the class used to implement the function
     * @param opcode identifies the function when a single class implements several functions
     * @param minArguments the minimum number of arguments required
     * @param maxArguments the maximum number of arguments allowed
     * @param itemType the item type of the result of the function
     * @param cardinality the cardinality of the result of the function
     * @param applicability the host languages (and versions of) in which this function is available
     * @return the entry describing the function. The entry is incomplete, it does not yet contain information
     * about the function arguments.
     */
    public static Entry makeEntry(String name, Class implementationClass, int opcode,
                                   int minArguments, int maxArguments,
                                   ItemType itemType, int cardinality, int applicability) {
        Entry e = new Entry();
        int hash = name.indexOf('#');
        if (hash < 0) {
            e.name = name;
        } else {
            e.name = name.substring(0, hash);
        }
        e.implementationClass = implementationClass;
        e.opcode = opcode;
        e.minArguments = minArguments;
        e.maxArguments = maxArguments;
        e.itemType = itemType;
        e.cardinality = cardinality;
        e.applicability = applicability;
        if (maxArguments > 100) {
            // special case for concat()
            e.argumentTypes = new SequenceType[1];
            e.resultIfEmpty = new Value[1];
        } else {
            e.argumentTypes = new SequenceType[maxArguments];
            e.resultIfEmpty = new Value[maxArguments];
        }
        return e;
    }

    /**
     * Add information to a function entry about the argument types of the function
     * @param e the entry for the function
     * @param a the position of the argument, counting from zero
     * @param type the item type of the argument
     * @param cardinality the cardinality of the argument
     * @param resultIfEmpty the value returned by the function if an empty sequence appears as the value
     * of this argument, in the case when this result is unaffected by any other arguments. Supply null
     * if this does not apply.
     */

    public static void arg(Entry e, int a, ItemType type, int cardinality, /*@Nullable*/ Value resultIfEmpty) {
        try {
            e.argumentTypes[a] = SequenceType.makeSequenceType(type, cardinality);
            e.resultIfEmpty[a] = resultIfEmpty;
        } catch (ArrayIndexOutOfBoundsException err) {
            System.err.println("Internal Saxon error: Can't set argument " + a + " of " + e.name);
        } 
    }

    private static HashMap<String, Entry> functionTable = new HashMap<String, Entry>(200);

    protected static ItemType SAME_AS_FIRST_ARGUMENT = NodeKindTest.NAMESPACE;
                // this could be any item type that is used only for this purpose

    static {
        Entry e;
        e = register("abs", Rounding.class, Rounding.ABS, 1, 1, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.NUMERIC, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("adjust-date-to-timezone", Adjust.class, 0, 1, 2, BuiltInAtomicType.DATE,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.DAY_TIME_DURATION,
                    StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("adjust-dateTime-to-timezone", Adjust.class, 0, 1, 2, BuiltInAtomicType.DATE_TIME,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.DAY_TIME_DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("adjust-time-to-timezone", Adjust.class, 0, 1, 2, BuiltInAtomicType.TIME,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.DAY_TIME_DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("avg", Average.class, 0, 1, 1, BuiltInAtomicType.ANY_ATOMIC,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
                // can't say "same as first argument" because the avg of a set of integers is decimal
            arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);

        e = register("base-uri", BaseURI.class, 0, 0, 1, BuiltInAtomicType.ANY_URI,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("boolean", BooleanFn.class, 0, 1, 1, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, null);

        e = register("ceiling", Rounding.class, Rounding.CEILING, 1, 1, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.NUMERIC, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("codepoint-equal", CodepointEqual.class, 0, 2, 2, BuiltInAtomicType.BOOLEAN,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("codepoints-to-string", CodepointsToString.class, 0, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_MORE, null);

        e = register("collection", Collection.class, 0, 0, 1, Type.NODE_TYPE,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("compare", Compare.class, 0, 2, 3, BuiltInAtomicType.INTEGER,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("concat", Concat.class, 0, 2, Integer.MAX_VALUE, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            // Note, this has a variable number of arguments so it is treated specially

        e = register("contains", Contains.class, 0, 2, 3, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, BooleanValue.TRUE);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);



        e = register("count", Count.class, 0, 1, 1, BuiltInAtomicType.INTEGER,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, Int64Value.ZERO);

            register("current", Current.class, 0, 0, 0, Type.ITEM_TYPE,
                    StaticProperty.EXACTLY_ONE, XSLT);

            register("current-date", CurrentDateTime.class, 0, 0, 0, BuiltInAtomicType.DATE,
                    StaticProperty.EXACTLY_ONE, CORE);

            register("current-dateTime", CurrentDateTime.class, 0, 0, 0, BuiltInAtomicType.DATE_TIME,
                    StaticProperty.EXACTLY_ONE, CORE);

            register("current-time", CurrentDateTime.class, 0, 0, 0, BuiltInAtomicType.TIME,
                    StaticProperty.EXACTLY_ONE, CORE);

            register("current-group", CurrentGroup.class, 0, 0, 0, Type.ITEM_TYPE,
                    StaticProperty.ALLOWS_ZERO_OR_MORE, XSLT);

            register("current-grouping-key", CurrentGroupingKey.class, 0, 0, 0,
                    BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_ONE, XSLT);

        e = register("data#0", Data.class, 0, 0, 1, BuiltInAtomicType.ANY_ATOMIC,
                StaticProperty.ALLOWS_ZERO, XPATH30);
            // declare argument that is added by the system
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            
        e = register("data#1", Data.class, 0, 1, 1, BuiltInAtomicType.ANY_ATOMIC,
                    StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
                arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);

        e = register("dateTime", DateTimeConstructor.class, 0, 2, 2, BuiltInAtomicType.DATE_TIME,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("day-from-date", Component.class, (Component.DAY<<16) + StandardNames.XS_DATE, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("day-from-dateTime", Component.class, (Component.DAY<<16) + StandardNames.XS_DATE_TIME, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("days-from-duration", Component.class, (Component.DAY<<16) + StandardNames.XS_DURATION, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
                    arg(e, 0, BuiltInAtomicType.DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("deep-equal", DeepEqual.class, 0, 2, 3, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, null);
            arg(e, 1, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

            register("default-collation", DefaultCollation.class, 0, 0, 0, BuiltInAtomicType.STRING,
                    StaticProperty.EXACTLY_ONE, CORE);

        e = register("distinct-values", DistinctValues.class, 0, 1, 2, BuiltInAtomicType.ANY_ATOMIC,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("doc", Doc.class, 0, 1, 1, NodeKindTest.DOCUMENT,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("doc-available", DocAvailable.class, 0, 1, 1, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, BooleanValue.FALSE);

        e = register("document", DocumentFn.class, 0, 1, 2, Type.NODE_TYPE,
                StaticProperty.ALLOWS_ZERO_OR_MORE, XSLT);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, null);
            arg(e, 1, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);

        e = register("document-uri#0", DocumentUriFn.class, 0, 0, 1, BuiltInAtomicType.ANY_URI,
                    StaticProperty.ALLOWS_ZERO_OR_ONE, XPATH30);
            // declare argument that is added by the system
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);


        e = register("document-uri#1", DocumentUriFn.class, 0, 1, 1, BuiltInAtomicType.ANY_URI,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("empty", Empty.class, 0, 1, 1, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, BooleanValue.TRUE);

        e = register("ends-with", EndsWith.class, 0, 2, 3, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, BooleanValue.TRUE);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("element-available", ElementAvailable.class, 0, 1, 1,
                BuiltInAtomicType.BOOLEAN, StaticProperty.EXACTLY_ONE, XSLT|USE_WHEN);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("element-with-id", Id.class, Id.ELEMENT_WITH_ID, 1, 2, NodeKindTest.ELEMENT, StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);

        e = register("encode-for-uri", EscapeURI.class, EscapeURI.ENCODE_FOR_URI, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("escape-html-uri", EscapeURI.class, EscapeURI.HTML_URI, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("error", Error.class, 0, 0, 3, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            // The return type is chosen so that use of the error() function will never give a static type error,
            // on the basis that item()? overlaps every other type, and it's almost impossible to make any
            // unwarranted inferences from it, except perhaps count(error()) lt 2.
            arg(e, 0, BuiltInAtomicType.QNAME, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, null);

        e = register("exactly-one", TreatFn.class, StaticProperty.EXACTLY_ONE, 1, 1, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.EXACTLY_ONE, null);
                // because we don't do draconian static type checking, we can do the work in the argument type checking code

        e = register("exists", Exists.class, 0, 1, 1, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, BooleanValue.FALSE);

//            register("false", BooleanFn.class, BooleanFn.FALSE, 0, 0, BuiltInAtomicType.BOOLEAN,
//                    StaticProperty.EXACTLY_ONE, CORE);

        e = register("floor", Rounding.class, Rounding.FLOOR, 1, 1, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.NUMERIC, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("format-date", FormatDate.class, StandardNames.XS_DATE, 2, 5, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, XSLT | XPATH30);
            arg(e, 0, BuiltInAtomicType.DATE, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 3, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 4, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("format-dateTime", FormatDate.class, StandardNames.XS_DATE_TIME, 2, 5, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, XSLT | XPATH30);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 3, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 4, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("format-number", FormatNumber.class, 0, 2, 3, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, XSLT | XPATH30);
            arg(e, 0, BuiltInAtomicType.NUMERIC, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("format-time", FormatDate.class, StandardNames.XS_TIME, 2, 5, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, XSLT | XPATH30);
            arg(e, 0, BuiltInAtomicType.TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 3, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 4, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("function-arity", FunctionArity.class, 0, 1, 1, BuiltInAtomicType.INTEGER,
                StaticProperty.EXACTLY_ONE, XPATH30);
            arg(e, 0, AnyFunctionType.getInstance(), StaticProperty.EXACTLY_ONE, null);

        e = register("function-available", FunctionAvailable.class, 0, 1, 2, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, XSLT|USE_WHEN);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 1, BuiltInAtomicType.INTEGER, StaticProperty.EXACTLY_ONE, null);

        e = register("function-name", FunctionName.class, 0, 1, 1, BuiltInAtomicType.QNAME,
                StaticProperty.ALLOWS_ZERO_OR_ONE, XPATH30);
            arg(e, 0, AnyFunctionType.getInstance(), StaticProperty.EXACTLY_ONE, null);

        e = register("generate-id", GenerateId.class, 0, 0, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, XSLT | XPATH30);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("hours-from-dateTime", Component.class, (Component.HOURS<<16) + StandardNames.XS_DATE_TIME, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("hours-from-duration", Component.class, (Component.HOURS<<16) + StandardNames.XS_DURATION, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
                    arg(e, 0, BuiltInAtomicType.DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("hours-from-time", Component.class, (Component.HOURS<<16) + StandardNames.XS_TIME, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("id", Id.class, Id.ID, 1, 2, NodeKindTest.ELEMENT, StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);

        e = register("idref", Idref.class, 0, 1, 2, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);

            register("implicit-timezone", CurrentDateTime.class, 0, 0, 0, BuiltInAtomicType.DAY_TIME_DURATION,
                    StaticProperty.EXACTLY_ONE, CORE);

        e = register("in-scope-prefixes", InScopePrefixes.class, 0, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, NodeKindTest.ELEMENT, StaticProperty.EXACTLY_ONE, null);

        e = register("index-of", IndexOf.class, 0, 2, 3, BuiltInAtomicType.INTEGER,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("insert-before", Insert.class, 0, 3, 3, Type.ITEM_TYPE,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, null);
            arg(e, 1, BuiltInAtomicType.INTEGER, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, null);

        e = register("iri-to-uri", EscapeURI.class, EscapeURI.IRI_TO_URI, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("key", KeyFn.class, 0, 2, 3, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, XSLT);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 1, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 2, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);

        e = register("lang", Lang.class, 0, 1, 2, BuiltInAtomicType.BOOLEAN, StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);

            register("last", Last.class, 0, 0, 0, BuiltInAtomicType.INTEGER, StaticProperty.EXACTLY_ONE, CORE);

        e = register("local-name", LocalNameFn.class, 0, 0, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("local-name-from-QName", Component.class, (Component.LOCALNAME<<16) + StandardNames.XS_QNAME, 1, 1,
                BuiltInAtomicType.NCNAME, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.QNAME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("lower-case", ForceCase.class, ForceCase.LOWERCASE, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("matches", Matches.class, 0, 2, 3, BuiltInAtomicType.BOOLEAN, StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("max", Minimax.class, Minimax.MAX, 1, 2, BuiltInAtomicType.ANY_ATOMIC,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("min", Minimax.class, Minimax.MIN, 1, 2, BuiltInAtomicType.ANY_ATOMIC,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("minutes-from-dateTime", Component.class, (Component.MINUTES<<16) + StandardNames.XS_DATE_TIME, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("minutes-from-duration", Component.class, (Component.MINUTES<<16) + StandardNames.XS_DURATION, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("minutes-from-time", Component.class, (Component.MINUTES<<16) + StandardNames.XS_TIME, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("month-from-date", Component.class, (Component.MONTH<<16) + StandardNames.XS_DATE, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("month-from-dateTime", Component.class, (Component.MONTH<<16) + StandardNames.XS_DATE_TIME, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("months-from-duration", Component.class, (Component.MONTH<<16) + StandardNames.XS_DURATION, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("name", NameFn.class, 0, 0, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("namespace-uri", NamespaceUriFn.class, 0, 0, 1, BuiltInAtomicType.ANY_URI,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("namespace-uri-for-prefix", NamespaceForPrefix.class, 0, 2, 2, BuiltInAtomicType.ANY_URI,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, NodeKindTest.ELEMENT, StaticProperty.EXACTLY_ONE, null);

        e = register("namespace-uri-from-QName", Component.class, (Component.NAMESPACE<<16) + StandardNames.XS_QNAME, 1, 1,
                BuiltInAtomicType.ANY_URI, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.QNAME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("nilled", Nilled.class, 0, 1, 1, BuiltInAtomicType.BOOLEAN,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("node-name#0", NodeNameFn.class, 0, 0, 1, BuiltInAtomicType.QNAME,
                         StaticProperty.ALLOWS_ZERO_OR_ONE, XPATH30);
            // entry matches a call with zero args, but an argument is supplied by the system
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("node-name#1", NodeNameFn.class, 0, 1, 1, BuiltInAtomicType.QNAME,
                 StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("not", NotFn.class, 0, 1, 1, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, BooleanValue.TRUE);

            register("normalize-space", NormalizeSpace.class, 0, 0, 1, BuiltInAtomicType.STRING,
                    StaticProperty.EXACTLY_ONE, CORE);
            register("normalize-space#0", NormalizeSpace.class, 0, 0, 0, BuiltInAtomicType.STRING,
                    StaticProperty.EXACTLY_ONE, CORE);

        e = register("normalize-space#1", NormalizeSpace.class, 0, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("normalize-unicode", NormalizeUnicode.class, 0, 1, 2, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("number", NumberFn.class, 0, 0, 1, BuiltInAtomicType.DOUBLE,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_ONE, DoubleValue.NaN);

        e = register("one-or-more", TreatFn.class, StaticProperty.ALLOWS_ONE_OR_MORE, 1, 1, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ONE_OR_MORE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ONE_OR_MORE, null);
                // because we don't do draconian static type checking, we can do the work in the argument type checking code

            register("position", Position.class, 0, 0, 0, BuiltInAtomicType.INTEGER,
                    StaticProperty.EXACTLY_ONE, CORE);

        e = register("prefix-from-QName", Component.class, (Component.PREFIX<<16) + StandardNames.XS_QNAME, 1, 1,
                BuiltInAtomicType.NCNAME, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.QNAME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("put", Put.class, 0, 2, 2, AnyItemType.getInstance(), StaticProperty.ALLOWS_ZERO_OR_ONE, XQUPDATE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("QName", QNameFn.class, 0, 2, 2, BuiltInAtomicType.QNAME, StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("regex-group", RegexGroup.class, 0, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, XSLT);
            arg(e, 0, BuiltInAtomicType.INTEGER, StaticProperty.EXACTLY_ONE, null);

        e = register("remove", Remove.class, 0, 2, 2, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, BuiltInAtomicType.INTEGER, StaticProperty.EXACTLY_ONE, null);

        e = register("replace", Replace.class, 0, 3, 4, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 3, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("resolve-QName", ResolveQName.class, 0, 2, 2, BuiltInAtomicType.QNAME,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, NodeKindTest.ELEMENT, StaticProperty.EXACTLY_ONE, null);

        e = register("resolve-uri", ResolveURI.class, 0, 1, 2, BuiltInAtomicType.ANY_URI,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("reverse", Reverse.class, 0, 1, 1, Type.ITEM_TYPE,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);

        e = register("root", Root.class, 0, 0, 1, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, Type.NODE_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            
        e = register("round#1", Rounding.class, Rounding.ROUND, 1, 1, SAME_AS_FIRST_ARGUMENT,
                    StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
        	arg(e, 0, BuiltInAtomicType.NUMERIC, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("round#2", Rounding.class, Rounding.ROUND, 2, 2, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_ONE, XPATH30);
            arg(e, 0, BuiltInAtomicType.NUMERIC, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.INTEGER, StaticProperty.EXACTLY_ONE, null);

        e = register("round-half-to-even", Rounding.class, Rounding.HALF_EVEN, 1, 2, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.NUMERIC, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.INTEGER, StaticProperty.EXACTLY_ONE, null);

        e = register("seconds-from-dateTime", Component.class, (Component.SECONDS<<16) + StandardNames.XS_DATE_TIME, 1, 1,
                BuiltInAtomicType.DECIMAL, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("seconds-from-duration", Component.class, (Component.SECONDS<<16) + StandardNames.XS_DURATION, 1, 1,
                BuiltInAtomicType.DECIMAL, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
                    arg(e, 0, BuiltInAtomicType.DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("seconds-from-time", Component.class, (Component.SECONDS<<16) + StandardNames.XS_TIME, 1, 1,
                BuiltInAtomicType.DECIMAL, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("starts-with", StartsWith.class, 0, 2, 3, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, BooleanValue.TRUE);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

            register("static-base-uri", StaticBaseURI.class, 0, 0, 0, BuiltInAtomicType.ANY_URI,
                    StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);

        e = register("string", StringFn.class, 0, 0, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

            register("string-length", StringLength.class, 0, 0, 1, BuiltInAtomicType.INTEGER,
                    StaticProperty.EXACTLY_ONE, CORE);
            register("string-length#0", StringLength.class, 0, 0, 0, BuiltInAtomicType.INTEGER,
                    StaticProperty.EXACTLY_ONE, CORE);

        e = register("string-length#1", StringLength.class, 0, 1, 1, BuiltInAtomicType.INTEGER,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("string-join#1", StringJoin.class, 0, 1, 2, BuiltInAtomicType.STRING,
                 StaticProperty.EXACTLY_ONE, XPATH30);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE, StringValue.EMPTY_STRING);

        e = register("string-join#2", StringJoin.class, 0, 2, 2, BuiltInAtomicType.STRING,
                    StaticProperty.EXACTLY_ONE, CORE);
                arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE, StringValue.EMPTY_STRING);
                arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("string-to-codepoints", StringToCodepoints.class, 0, 1, 1, BuiltInAtomicType.INTEGER,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("subsequence", Subsequence.class, 0, 2, 3, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);
            arg(e, 1, BuiltInAtomicType.NUMERIC, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.NUMERIC, StaticProperty.EXACTLY_ONE, null);

        e = register("substring", Substring.class, 0, 2, 3, BuiltInAtomicType.STRING,

                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);
            arg(e, 1, BuiltInAtomicType.NUMERIC, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.NUMERIC, StaticProperty.EXACTLY_ONE, null);

        e = register("substring-after", SubstringAfter.class, 0, 2, 3, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("substring-before", SubstringBefore.class, 0, 2, 3, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("sum", Sum.class, 0, 1, 2, BuiltInAtomicType.ANY_ATOMIC,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_MORE, null);
            arg(e, 1, BuiltInAtomicType.ANY_ATOMIC, StaticProperty.ALLOWS_ZERO_OR_ONE, null);

        e = register("system-property", SystemProperty.class, 0, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, XSLT|USE_WHEN);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("timezone-from-date", Component.class, (Component.TIMEZONE<<16) + StandardNames.XS_DATE, 1, 1,
                BuiltInAtomicType.DAY_TIME_DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("timezone-from-dateTime", Component.class, (Component.TIMEZONE<<16) + StandardNames.XS_DATE_TIME, 1, 1,
                BuiltInAtomicType.DAY_TIME_DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("timezone-from-time", Component.class, (Component.TIMEZONE<<16) + StandardNames.XS_TIME, 1, 1,
                BuiltInAtomicType.DAY_TIME_DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
                    arg(e, 0, BuiltInAtomicType.TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("trace", Trace.class, 0, 2, 2, SAME_AS_FIRST_ARGUMENT, StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

//            register("true", BooleanFn.class, BooleanFn.TRUE, 0, 0, BuiltInAtomicType.BOOLEAN,
//                    StaticProperty.EXACTLY_ONE, CORE);

        e = register("translate", Translate.class, 0, 3, 3, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("tokenize", Tokenize.class, 0, 2, 3, BuiltInAtomicType.STRING,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 2, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("type-available", TypeAvailable.class, 0, 1, 1, BuiltInAtomicType.BOOLEAN,
                StaticProperty.EXACTLY_ONE, XSLT|USE_WHEN);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("unordered", Unordered.class, 0, 1, 1, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_MORE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_MORE, EMPTY);

        e = register("upper-case", ForceCase.class, ForceCase.UPPERCASE, 1, 1, BuiltInAtomicType.STRING,
                StaticProperty.EXACTLY_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, StringValue.EMPTY_STRING);

        e = register("unparsed-entity-uri", UnparsedEntity.class, UnparsedEntity.URI, 1, 1, BuiltInAtomicType.ANY_URI,
                StaticProperty.EXACTLY_ONE, XSLT);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        // internal version of unparsed-entity-uri with second argument representing the current document
        e = register("unparsed-entity-uri_9999_", UnparsedEntity.class, UnparsedEntity.URI, 2, 2,
                BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, INTERNAL);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 1, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);
                    // it must actually be a document node, but there's a non-standard error code

        e = register("unparsed-entity-public-id", UnparsedEntity.class, UnparsedEntity.PUBLIC_ID, 1, 1,
                BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, XSLT);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        // internal version of unparsed-entity-public-id with second argument representing the current document
        e = register("unparsed-entity-public-id_9999_", UnparsedEntity.class, UnparsedEntity.PUBLIC_ID, 2, 2,
                BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, INTERNAL);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 1, Type.NODE_TYPE, StaticProperty.EXACTLY_ONE, null);
                    // it must actually be a document node, but there's a non-standard error code

        e = register("unparsed-text", UnparsedText.class, 0, 1, 2,
                BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, XSLT|XPATH30);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("unparsed-text-available", UnparsedTextAvailable.class, 0, 1, 2,
                BuiltInAtomicType.BOOLEAN, StaticProperty.EXACTLY_ONE, XSLT|XPATH30);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("unparsed-text-lines", UnparsedTextLines.class, 0, 1, 2,
                BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_MORE, XSLT30);
            arg(e, 0, BuiltInAtomicType.STRING, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
            arg(e, 1, BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE, null);

        e = register("year-from-date", Component.class, (Component.YEAR<<16) + StandardNames.XS_DATE, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("year-from-dateTime", Component.class, (Component.YEAR<<16) + StandardNames.XS_DATE_TIME, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DATE_TIME, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("years-from-duration", Component.class, (Component.YEAR<<16) + StandardNames.XS_DURATION, 1, 1,
                BuiltInAtomicType.INTEGER, StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, BuiltInAtomicType.DURATION, StaticProperty.ALLOWS_ZERO_OR_ONE, EMPTY);

        e = register("zero-or-one", TreatFn.class, StaticProperty.ALLOWS_ZERO_OR_ONE, 1, 1, SAME_AS_FIRST_ARGUMENT,
                StaticProperty.ALLOWS_ZERO_OR_ONE, CORE);
            arg(e, 0, Type.ITEM_TYPE, StaticProperty.ALLOWS_ZERO_OR_ONE, null);
                // because we don't do draconian static type checking, we can do the work in the argument type checking code
    }


    /**
     * Get the table entry for the function with a given name
     * @param name the name of the function. This may be an unprefixed local-name for functions in the
     * system namespace, or may use the conventional prefix "saxon:" in the case of Saxon extension functions
     * that are specially recognized
     * @param arity the number of arguments of the function
     * @return if the function name is known, an Entry containing information about the function. Otherwise,
     * null
     */

    public static Entry getFunction(String name, int arity) {
        // try first for an entry of the form name#arity
        Entry e = functionTable.get(name + '#' + arity);
        if (e != null) {
            return e;
        }
        // try for a generic entry
        return functionTable.get(name);
    }

    /**
     * An entry in the table describing the properties of a function
     */
    public static class Entry implements java.io.Serializable {
        /**
         * The name of the function: a local name in the case of functions in the standard library, or a
         * name with the conventional prefix "saxon:" in the case of Saxon extension functions
         */
        public String name;
        /**
         * The class containing the implementation of this function (always a subclass of SystemFunction)
         */
        public Class implementationClass;
        /**
         * Some classes support more than one function. In these cases the particular function is defined
         * by an integer opcode, whose meaning is local to the implementation class.
         */
        public int opcode;
        /**
         * The minimum number of arguments required
         */
        public int minArguments;
        /**
         * The maximum number of arguments permitted
         */
        public int maxArguments;
        /**
         * The item type of the result of the function
         */
        public ItemType itemType;
        /**
         * The cardinality of the result of the function
         */
        public int cardinality;
        /**
         * Flags indicating which host languages the function is applicable to
         */
        public int applicability;
        /**
         * An array holding the types of the arguments to the function
         */
        public SequenceType[] argumentTypes;
        /**
         * An array holding, for each declared argument, the value that is to be returned if an empty sequence
         * as the value of this argument allows the result to be determined irrespective of the values of the
         * other arguments; null if there is no such calculation possible
         */
        public Value[] resultIfEmpty;
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