package net.sf.saxon.expr;

import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.PromotionOffer;
import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Value;

/**
 * Variable reference: a reference to a variable. This may be an XSLT-defined variable, a range
 * variable defined within the XPath expression, or a variable defined in some other static context.
 */

public class VariableReference extends Expression implements BindingReference {

    /*@Nullable*/ protected Binding binding = null;     // This will be null until fixup() is called; it will also be null
                                // if the variable reference has been inlined
    protected SequenceType staticType = null;
    protected Value constantValue = null;
    transient String displayName = null;
    private boolean flattened = false;
    private boolean inLoop = true;
    private boolean filtered = false;

    /**
     * Create a Variable Reference
     */

    public VariableReference() {
        //System.err.println("Creating varRef");
    }

    /**
     * Create a Variable Reference
     * @param binding the variable binding to which this variable refers
     */

    public VariableReference(Binding binding) {
        //System.err.println("Creating varRef1");
        displayName = binding.getVariableQName().getDisplayName();
        fixup(binding);
    }

    /**
     * Create a clone copy of this VariableReference
     * @return the cloned copy
     */

    /*@NotNull*/
    public Expression copy() {
        if (binding == null) {
            //System.err.println("copy unbound variable " + this);
            throw new UnsupportedOperationException("Cannot copy a variable reference whose binding is unknown");
        }
        VariableReference ref = new VariableReference();
        ref.binding = binding;
        ref.staticType = staticType;
        ref.constantValue = constantValue;
        ref.displayName = displayName;
        binding.addReference(inLoop);
        ExpressionTool.copyLocationInfo(this, ref);
        return ref;
    }

    /**
     * Set static type. This is a callback from the variable declaration object. As well
     * as supplying the static type, it may also supply a compile-time value for the variable.
     * As well as the type information, other static properties of the value are supplied:
     * for example, whether the value is an ordered node-set.
     * @param type the static type of the variable
     * @param value the value of the variable if this is a compile-time constant, or null otherwise
     * @param properties static properties of the expression to which the variable is bound
     */

    public void setStaticType(SequenceType type, /*@Nullable*/ Value value, int properties) {
        // System.err.println(this + " Set static type = " + type);
        staticType = type;
        constantValue = value;
        // Although the variable may be a context document node-set at the point it is defined,
        // the context at the point of use may be different, so this property cannot be transferred.
        int dependencies = getDependencies();
        staticProperties = (properties & ~StaticProperty.CONTEXT_DOCUMENT_NODESET) |
                StaticProperty.NON_CREATIVE |
                type.getCardinality() |
                dependencies;
    }

    /**
     * Mark an expression as being "flattened". This is a collective term that includes extracting the
     * string value or typed value, or operations such as simple value construction that concatenate text
     * nodes before atomizing. The implication of all of these is that although the expression might
     * return nodes, the identity of the nodes has no significance. This is called during type checking
     * of the parent expression. At present, only variable references take any notice of this notification.
     */

    public void setFlattened(boolean flattened) {
        this.flattened = flattened;
    }

    /**
     * Test whether this variable reference is flattened - that is, whether it is atomized etc
     * @return true if the value of the variable is atomized, or converted to a string or number
     */

    public boolean isFlattened() {
        return flattened;
    }

    /**
     * Mark an expression as filtered: that is, it appears as the base expression in a filter expression.
     * This notification currently has no effect except when the expression is a variable reference.
     */

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    /**
     * Determine whether this variable reference is filtered
     * @return true if the value of the variable is filtered by a predicate
     */

    public boolean isFiltered() {
        return filtered;
    }

    /**
     * Determine whether this variable reference appears in a loop relative to its declaration.
     * By default, when in doubt, returns true. This is calculated during type-checking.
     * @return true if this variable reference occurs in a loop, where the variable declaration is
     * outside the loop
     */

    public boolean isInLoop() {
        return inLoop;
    }

    /**
     * Type-check the expression. At this stage details of the static type must be known.
     * If the variable has a compile-time value, this is substituted for the variable reference
     */

    /*@NotNull*/
    public Expression typeCheck(ExpressionVisitor visitor, ExpressionVisitor.ContextItemType contextItemType) throws XPathException {
        if (constantValue != null) {
            binding = null;
            return Literal.makeLiteral(constantValue);
        }
//        if (staticType == null) {
//            throw new IllegalStateException("Variable $" + getDisplayName() + " has not been fixed up");
//        }


        inLoop = visitor.isLoopingReference(binding, this);
// following code removed because it causes error181 to blow the stack - need to check for circularities well            
//            if (binding instanceof GlobalVariable) {
//                ((GlobalVariable)binding).typeCheck(visitor, AnyItemType.getInstance());
//            }

        if (binding != null) {
            binding.addReference(inLoop);
        }

        return this;
    }

    /**
     * Type-check the expression. At this stage details of the static type must be known.
     * If the variable has a compile-time value, this is substituted for the variable reference
     */

    /*@NotNull*/
    public Expression optimize(ExpressionVisitor visitor, ExpressionVisitor.ContextItemType contextItemType) throws XPathException {
        if (constantValue != null) {
            binding = null;
            return Literal.makeLiteral(constantValue);
        }

        return this;
    }



    /**
     * Fix up this variable reference to a Binding object, which enables the value of the variable
     * to be located at run-time.
     */

    public void fixup(Binding binding) {
        this.binding = binding;
        resetLocalStaticProperties();
    }

    /**
     * Provide additional information about the type of the variable, typically derived by analyzing
     * the initializer of the variable binding
     * @param type the item type of the variable
     * @param cardinality the cardinality of the variable
     * @param constantValue the actual value of the variable, if this is known statically, otherwise null
     * @param properties additional static properties of the variable's initializer
     * @param visitor an ExpressionVisitor
     */

    public void refineVariableType(
            ItemType type, int cardinality, /*@Nullable*/ Value constantValue, int properties, ExpressionVisitor visitor) {
        Executable exec = visitor.getExecutable();
        if (exec == null) {
            // happens during use-when evaluation
            return;
        } 
        TypeHierarchy th = exec.getConfiguration().getTypeHierarchy();
        ItemType oldItemType = getItemType(th);
        ItemType newItemType = oldItemType;
        if (th.isSubType(type, oldItemType)) {
            newItemType = type;
        }
        if (oldItemType instanceof NodeTest && type instanceof AtomicType) {
            // happens when all references are flattened
            newItemType = type;
        }
        int newcard = cardinality & getCardinality();
        if (newcard==0) {
            // this will probably lead to a type error later
            newcard = getCardinality();
        }
        SequenceType seqType = SequenceType.makeSequenceType(newItemType, newcard);
        setStaticType(seqType, constantValue, properties);
    }

    /**
     * Determine the data type of the expression, if possible
     *
     * @param th the type hierarchy cache
     * @return the type of the variable, if this can be determined statically;
     *         otherwise Type.ITEM (meaning not known in advance)
     */

    /*@NotNull*/
    public ItemType getItemType(TypeHierarchy th) {
        if (staticType == null || staticType.getPrimaryType() == AnyItemType.getInstance()) {
            if (binding != null) {
                return binding.getRequiredType().getPrimaryType();
            }
            return AnyItemType.getInstance();
        } else {
            return staticType.getPrimaryType();
        }
    }

    /**
     * For an expression that returns an integer or a sequence of integers, get
     * a lower and upper bound on the values of the integers that may be returned, from
     * static analysis. The default implementation returns null, meaning "unknown" or
     * "not applicable". Other implementations return an array of two IntegerValue objects,
     * representing the lower and upper bounds respectively. The values
     * UNBOUNDED_LOWER and UNBOUNDED_UPPER are used by convention to indicate that
     * the value may be arbitrarily large. The values MAX_STRING_LENGTH and MAX_SEQUENCE_LENGTH
     * are used to indicate values limited by the size of a string or the size of a sequence.
     *
     * @return the lower and upper bounds of integer values in the result, or null to indicate
     *         unknown or not applicable.
     */
    @Override
    public IntegerValue[] getIntegerBounds() {
        if (binding != null) {
            return binding.getIntegerBoundsForVariable();
        } else {
            return null;
        }
    }

    /**
     * Get the static cardinality
     */

    public int computeCardinality() {
        if (staticType == null) {
            if (binding == null) {
                return StaticProperty.ALLOWS_ZERO_OR_MORE;
            } else if (binding instanceof LetExpression) {
                return binding.getRequiredType().getCardinality();
            } else if (binding instanceof Assignation) {
                return StaticProperty.EXACTLY_ONE;
            } else {
                return binding.getRequiredType().getCardinality();
            }
        } else {
            return staticType.getCardinality();
        }
    }

    /**
     * Determine the special properties of this expression
     *
     * @return {@link StaticProperty#NON_CREATIVE} (unless the variable is assignable using saxon:assign)
     */

    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        if (binding == null || !binding.isAssignable()) {
            // if the variable reference is assignable, we mustn't move it, or any expression that contains it,
            // out of a loop. The way to achieve this is to treat it as a "creative" expression, because the
            // optimizer recognizes such expressions and handles them with care...
            p |= StaticProperty.NON_CREATIVE;
        }
        if (binding instanceof Assignation) {
            Expression exp = ((Assignation)binding).getSequence();
            if (exp != null) {
                p |= (exp.getSpecialProperties() & StaticProperty.NOT_UNTYPED_ATOMIC);
            }
        }
        if (staticType != null &&
                !Cardinality.allowsMany(staticType.getCardinality()) &&
                staticType.getPrimaryType() instanceof NodeTest) {
            p |= StaticProperty.SINGLE_DOCUMENT_NODESET;
        }
        return p;
    }

    /**
     * Test if this expression is the same as another expression.
     * (Note, we only compare expressions that
     * have the same static and dynamic context).
     */

    public boolean equals(Object other) {
        return (other instanceof VariableReference &&
                binding == ((VariableReference) other).binding &&
                binding != null);
    }

    /**
     * get HashCode for comparing two expressions
     */

    public int hashCode() {
        return binding == null ? 73619830 : binding.hashCode();
    }


    public int getIntrinsicDependencies() {
        int d = 0;
        if (binding == null) {
            // assume the worst
            d |= (StaticProperty.DEPENDS_ON_LOCAL_VARIABLES |
                    StaticProperty.DEPENDS_ON_ASSIGNABLE_GLOBALS |
                    StaticProperty.DEPENDS_ON_RUNTIME_ENVIRONMENT);
        } else if (binding.isGlobal()) {
            if (binding.isAssignable()) {
                d |= StaticProperty.DEPENDS_ON_ASSIGNABLE_GLOBALS;
            }
            if (binding instanceof GlobalParam) {
                d |= StaticProperty.DEPENDS_ON_RUNTIME_ENVIRONMENT;
            }
        } else {
            d |= StaticProperty.DEPENDS_ON_LOCAL_VARIABLES;
        }
        return d;
    }

    /**
     * Promote this expression if possible
     */

    public Expression promote(PromotionOffer offer, Expression parent) throws XPathException {
        if (offer.action == PromotionOffer.INLINE_VARIABLE_REFERENCES) {
            Expression exp = offer.accept(parent, this);
            if (exp != null) {
                // Replace the variable reference with the given expression.
                offer.accepted = true;
                return exp;
            }
        }
        return this;
    }

    /**
     * An implementation of Expression must provide at least one of the methods evaluateItem(), iterate(), or process().
     * This method indicates which of these methods is provided. This implementation provides both all three methods
     * natively.
     */

    public int getImplementationMethod() {
        return (Cardinality.allowsMany(getCardinality()) ? 0 : EVALUATE_METHOD)
                | ITERATE_METHOD | PROCESS_METHOD;
    }


    /**
     * Add a representation of this expression to a PathMap. The PathMap captures a map of the nodes visited
     * by an expression in a source tree.
     * <p/>
     * <p>The default implementation of this method assumes that an expression does no navigation other than
     * the navigation done by evaluating its subexpressions, and that the subexpressions are evaluated in the
     * same context as the containing expression. The method must be overridden for any expression
     * where these assumptions do not hold. For example, implementations exist for AxisExpression, ParentExpression,
     * and RootExpression (because they perform navigation), and for the doc(), document(), and collection()
     * functions because they create a new navigation root. Implementations also exist for PathExpression and
     * FilterExpression because they have subexpressions that are evaluated in a different context from the
     * calling expression.</p>
     *
     * @param pathMap        the PathMap to which the expression should be added
     * @param pathMapNodeSet the PathMapNodeSet to which the paths embodied in this expression should be added
     * @return the pathMapNodeSet representing the points in the source document that are both reachable by this
     *         expression, and that represent possible results of this expression. For an expression that does
     *         navigation, it represents the end of the arc in the path map that describes the navigation route. For other
     *         expressions, it is the same as the input pathMapNode.
     */

    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        return pathMap.getPathForVariable(getBinding());
    }

    /**
     * Get the value of this variable in a given context.
     *
     * @param c the XPathContext which contains the relevant variable bindings
     * @return the value of the variable, if it is defined
     * @throws XPathException if the variable is undefined
     */

    /*@NotNull*/
    public SequenceIterator<? extends Item> iterate(XPathContext c) throws XPathException {
        try {
            ValueRepresentation<? extends Item> actual = evaluateVariable(c);
            return Value.asIterator(actual);
        } catch (XPathException err) {
            err.maybeSetLocation(this);
            throw err;
        } catch (AssertionError err) {
            //err.printStackTrace();
            String msg = err.getMessage() + ". Variable reference $" + getDisplayName() +
                    " at line " + getLineNumber() + (getSystemId() == null ? "" : " of " + getSystemId());
            StandardErrorListener.printStackTrace(System.err, c);
            throw new AssertionError(msg);
        }
    }

    public Item evaluateItem(XPathContext c) throws XPathException {
        try {
            ValueRepresentation actual = evaluateVariable(c);
            if (actual instanceof Item) {
                return (Item) actual;
            }
            return Value.asItem(actual);
        } catch (XPathException err) {
            err.maybeSetLocation(this);
            throw err;
        }
    }

    public void process(XPathContext c) throws XPathException {
        try {
            ValueRepresentation actual = evaluateVariable(c);
            Value.asValue(actual).process(c);
        } catch (XPathException err) {
            err.maybeSetLocation(this);
            throw err;
        }
    }

    /**
     * Evaluate this variable
     * @param c the XPath dynamic context
     * @return the value of the variable
     * @throws XPathException if any error occurs
     */

    /*@NotNull*/
    public ValueRepresentation<? extends Item> evaluateVariable(XPathContext c) throws XPathException {
        try {
            return binding.evaluateVariable(c);
        } catch (NullPointerException err) {
            if (binding == null) {
                throw new IllegalStateException("Variable $" + displayName + " has not been fixed up");
            } else {
                throw err;
            }
        }
    }

    /**
     * Get the object bound to the variable
     * @return the Binding which declares this variable and associates it with a value
     */

    public Binding getBinding() {
        return binding;
    }

    /**
     * Get the display name of the variable. This is taken from the variable binding if possible
     * @return the display name (a lexical QName
     */

    public String getDisplayName() {
        if (binding != null) {
            return binding.getVariableQName().getDisplayName();
        } else {
            return displayName;
        }
    }

    /**
     * The toString() method for an expression attempts to give a representation of the expression
     * in an XPath-like form, but there is no guarantee that the syntax will actually be true XPath.
     * In the case of XSLT instructions, the toString() method gives an abstracted view of the syntax
     */

    public String toString() {
        String d = getDisplayName();
        return "$" + (d == null ? "$" : d);
    }

    /**
     * Diagnostic print of expression structure. The abstract expression tree
     * is written to the supplied output destination.
     */

    public void explain(ExpressionPresenter destination) {

        destination.startElement("variableReference");
        String d = getDisplayName();
        destination.emitAttribute("name", (d == null ? "null" : getDisplayName()));
        destination.emitAttribute("slot", ""+binding.getLocalSlotNumber());
        destination.endElement();
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