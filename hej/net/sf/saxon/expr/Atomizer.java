package net.sf.saxon.expr;

import net.sf.saxon.event.ReceiverOptions;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.om.FunctionItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.EmptySequenceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.*;
import net.sf.saxon.type.*;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SingletonItem;
import net.sf.saxon.value.Value;

/**
* An Atomizer is an expression corresponding essentially to the fn:data() function: it
* maps a sequence by replacing nodes with their typed values
*/

public final class Atomizer extends UnaryExpression  {

    private boolean untyped = false;       //set to true if it is known that the nodes being atomized will be untyped
    private boolean singleValued = false; // set to true if all atomized nodes will atomize to a single atomic value
    /*@Nullable*/ private ItemType operandItemType = null;

    /**
     * Constructor
     * @param sequence the sequence to be atomized
     *  */

    public Atomizer(Expression sequence) {
        super(sequence);
        sequence.setFlattened(true);
    }

    /**
     * An implementation of Expression must provide at least one of the methods evaluateItem(), iterate(), or process().
     * This method indicates which of these methods is provided directly. The other methods will always be available
     * indirectly, using an implementation that relies on one of the other methods.
     * @return the implementation method, for example {@link #ITERATE_METHOD} or {@link #EVALUATE_METHOD} or
     *         {@link #PROCESS_METHOD}
     */

    public int getImplementationMethod() {
        return super.getImplementationMethod() | WATCH_METHOD;
    }

    /**
    * Simplify an expression
     * @param visitor an expression visitor
     */

    /*@NotNull*/
    public Expression simplify(ExpressionVisitor visitor) throws XPathException {
        untyped = !visitor.getExecutable().isSchemaAware();
        computeSingleValued(visitor.getConfiguration().getTypeHierarchy());
        operand = visitor.simplify(operand);
        if (operand instanceof Literal) {
            Value val = ((Literal)operand).getValue();
            if (val instanceof SingletonItem && ((SingletonItem)val).asItem() instanceof FunctionItem) {
                XPathException err = new XPathException("Cannot atomize a function item", "FOTY0013");
                err.setLocator(this);
                throw err;
            }
            if (val instanceof AtomicValue) {
                return operand;
            }
            SequenceIterator iter = val.iterate();
            while (true) {
                // if all items in the sequence are atomic (they generally will be, since this is
                // done at compile time), then return the sequence
                Item i = iter.next();
                if (i == null) {
                    return operand;
                }
                if (i instanceof NodeInfo) {
                    return this;
                }
            }
        } else if (operand instanceof ValueOf && (((ValueOf)operand).getOptions()& ReceiverOptions.DISABLE_ESCAPING) == 0) {
            // XSLT users tend to use ValueOf unnecessarily
            return ((ValueOf)operand).convertToCastAsString();
        }
        return this;
    }

    /**
    * Type-check the expression
    */

    /*@NotNull*/
    public Expression typeCheck(ExpressionVisitor visitor, ExpressionVisitor.ContextItemType contextItemType) throws XPathException {
        untyped = !visitor.getExecutable().isSchemaAware();
        operand = visitor.typeCheck(operand, contextItemType);
        // If the configuration allows typed data, check whether the content type of these particular nodes is untyped
        final TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        computeSingleValued(th);
        visitor.resetStaticProperties();
        ItemType operandType = operand.getItemType(th);
        if (th.isSubType(operandType, BuiltInAtomicType.ANY_ATOMIC)) {
            return operand;
        }
        if (!operandType.isAtomizable()) {
            XPathException err;
            if (operandType instanceof FunctionItemType) {
                err = new XPathException(
                        "Cannot atomize a function item", "FOTY0013");
            } else {
                err = new XPathException(
                        "Cannot atomize an element that is defined in the schema to have element-only content", "FOTY0012");
            }
            err.setLocator(this);
            throw err;
        }
        operand.setFlattened(true);
        return this;
    }

    private void computeSingleValued(TypeHierarchy th) {
        singleValued = untyped;
        if (!singleValued) {
            ItemType nodeType = operand.getItemType(th);
            if (nodeType instanceof NodeTest) {
                SchemaType st = ((NodeTest)nodeType).getContentType();
                if (st == Untyped.getInstance() || st.isAtomicType() || (st.isComplexType() && st != AnyType.getInstance())) {
                    singleValued = true;
                }
                int mask = ((NodeTest)nodeType).getNodeKindMask();
                if ((mask & (1<<Type.ELEMENT | 1<<Type.ATTRIBUTE)) == 0) {
                    singleValued = true;
                }
            }
        }
    }


    /**
     * Perform optimisation of an expression and its subexpressions.
     * <p/>
     * <p>This method is called after all references to functions and variables have been resolved
     * to the declaration of the function or variable, and after all type checking has been done.</p>
     *
     * @param visitor         an expression visitor
     * @param contextItemType the static type of "." at the point where this expression is invoked.
     *                        The parameter is set to null if it is known statically that the context item will be undefined.
     *                        If the type of the context item is not known statically, the argument is set to
     *                        {@link net.sf.saxon.type.Type#ITEM_TYPE}
     * @return the original expression, rewritten if appropriate to optimize execution
     * @throws net.sf.saxon.trans.XPathException
     *          if an error is discovered during this phase
     *          (typically a type error)
     */

    /*@NotNull*/
    public Expression optimize(ExpressionVisitor visitor, ExpressionVisitor.ContextItemType contextItemType) throws XPathException {
        Expression exp = super.optimize(visitor, contextItemType);
        if (exp == this) {
            final TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            if (th.isSubType(operand.getItemType(th), BuiltInAtomicType.ANY_ATOMIC)) {
                return operand;
            }
            if (operand instanceof ValueOf && (((ValueOf)operand).getOptions()& ReceiverOptions.DISABLE_ESCAPING) == 0) {
                // XSLT users tend to use ValueOf unnecessarily
                Expression cast = ((ValueOf)operand).convertToCastAsString();
                return cast.optimize(visitor, contextItemType);
            }
            if (operand instanceof Block) {
                // replace atomize((x,y,z)) by (atomize(x), atomize(y), atomize(z)) as some of the atomizers
                // may prove to be redundant. (Also, it helps streaming)
                Expression[] children = ((Block)operand).getChildren();
                Expression[] atomizedChildren = new Expression[children.length];
                for (int i=0; i<children.length; i++) {
                    atomizedChildren[i] = new Atomizer(children[i]);
                }
                Block newBlock = new Block();
                newBlock.setChildren(atomizedChildren);
                return newBlock.typeCheck(visitor, contextItemType).optimize(visitor, contextItemType);
            }
        }
        return exp;
    }

    /**
     * Ask whether it is known that any nodes in the input will always be untyped
     * @return true if it is known that all nodes in the input will be untyped
     */
    public boolean isUntyped() {
        return untyped;
    }


    /**
     * Determine the special properties of this expression
     * @return {@link StaticProperty#NON_CREATIVE}.
     */

    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | StaticProperty.NON_CREATIVE;
    }

    /**
     * Copy an expression. This makes a deep copy.
     *
     * @return the copy of the original expression
     */

    /*@NotNull*/
    public Expression copy() {
        Atomizer copy = new Atomizer(getBaseExpression().copy());
        copy.untyped = untyped;
        copy.singleValued = singleValued;
        return copy;
    }

    /**
    * Iterate over the sequence of values
    */

    /*@NotNull*/
    public SequenceIterator<? extends AtomicValue> iterate(XPathContext context) throws XPathException {
        SequenceIterator base = operand.iterate(context);
        return getAtomizingIterator(base, untyped);
    }

    /**
    * Evaluate as an Item. This should only be called if the Atomizer has cardinality zero-or-one,
    * which will only be the case if the underlying expression has cardinality zero-or-one.
    */

    public Item evaluateItem(XPathContext context) throws XPathException {
        Item i = operand.evaluateItem(context);
        if (i==null) {
            return null;
        }
        if (i instanceof NodeInfo) {
            return Value.asItem(((NodeInfo)i).atomize());
        } else {
            return i;
        }
    }

    /**
     * Process the instruction, without returning any tail calls
     * @param context The dynamic context, giving access to the current node,
     *                the current variables, etc.
     */

    public void process(XPathContext context) throws XPathException {
        super.process(context);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
    * Determine the data type of the items returned by the expression, if possible
    * @return a value such as Type.STRING, Type.BOOLEAN, Type.NUMBER. For this class, the
     * result is always an atomic type, but it might be more specific.
     * @param th the type hierarchy cache
     */

	/*@NotNull*/
    public ItemType getItemType(TypeHierarchy th) {
        operandItemType = operand.getItemType(th);
        return getAtomizedItemType(operand, untyped, th);
    }

    /**
     * Compute the type that will result from atomizing the result of a given expression
     * @param operand the given expression
     * @param alwaysUntyped true if it is known that nodes will always be untyped
     * @param th the type hierarchy cache
     * @return the item type of the result of evaluating the operand expression, after atomization
     */

    public static ItemType getAtomizedItemType(Expression operand, boolean alwaysUntyped, TypeHierarchy th) {
        ItemType in = operand.getItemType(th);
        if (in.isPlainType()) {
            return in;
        }
        if (in instanceof NodeTest) {

            if (in instanceof EmptySequenceTest) {
                return in;
            }
            int kinds = ((NodeTest)in).getNodeKindMask();
            if (alwaysUntyped) {
                // Some node-kinds always have a typed value that's a string

                if ((kinds | STRING_KINDS) == STRING_KINDS) {
                    return BuiltInAtomicType.STRING;
                }
                // Some node-kinds are always untyped atomic; some are untypedAtomic provided that the configuration
                // is untyped

                if ((kinds | UNTYPED_IF_UNTYPED_KINDS) == UNTYPED_IF_UNTYPED_KINDS) {
                    return BuiltInAtomicType.UNTYPED_ATOMIC;
                }
            } else {
                if ((kinds | UNTYPED_KINDS) == UNTYPED_KINDS) {
                    return BuiltInAtomicType.UNTYPED_ATOMIC;
                }
            }

            return in.getAtomizedItemType();
        }
	    return BuiltInAtomicType.ANY_ATOMIC;
	}

    /**
     * Node kinds whose typed value is always a string
     */
    private static final int STRING_KINDS =
            (1<<Type.NAMESPACE) | (1<<Type.COMMENT) | (1<<Type.PROCESSING_INSTRUCTION);

    /**
     * Node kinds whose typed value is always untypedAtomic
     */

    private static final int UNTYPED_KINDS =
            (1<<Type.TEXT) | (1<<Type.DOCUMENT);

    /**
     * Node kinds whose typed value is untypedAtomic if the configuration is untyped
     */

    private static final int UNTYPED_IF_UNTYPED_KINDS =
            (1<<Type.TEXT) | (1<<Type.ELEMENT) | (1<<Type.DOCUMENT) | (1<<Type.ATTRIBUTE);

	/**
	* Determine the static cardinality of the expression
	*/

	public int computeCardinality() {
        if (untyped || singleValued) {
            return operand.getCardinality();
        } else {
            if (Cardinality.allowsMany(operand.getCardinality())) {
                return StaticProperty.ALLOWS_ZERO_OR_MORE;
            }
            ItemType in = operandItemType;
            if (in == null) {
                try {
                    in = operand.getItemType(getContainer().getExecutable().getConfiguration().getTypeHierarchy());
                } catch (NullPointerException npe) {
                    return StaticProperty.ALLOWS_ZERO_OR_MORE;
                }
            }
            if (in.isPlainType()) {
                return operand.getCardinality();
            }
            if (in instanceof NodeTest) {
                SchemaType schemaType = ((NodeTest)in).getContentType();
                if (schemaType.isAtomicType()) {
                    // can return at most one atomic value per node
                    return operand.getCardinality();
                }
            }
            return StaticProperty.ALLOWS_ZERO_OR_MORE;
        }
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
        PathMap.PathMapNodeSet result = operand.addToPathMap(pathMap, pathMapNodeSet);
        if (result != null) {
            TypeHierarchy th = getExecutable().getConfiguration().getTypeHierarchy();
            ItemType operandItemType = operand.getItemType(th);
            if (th.relationship(NodeKindTest.ELEMENT, operandItemType) != TypeHierarchy.DISJOINT ||
                    th.relationship(NodeKindTest.DOCUMENT, operandItemType) != TypeHierarchy.DISJOINT) {
                result.setAtomized();
            }
        }
        return null;
    }

    /**
     * Get an iterator that returns the result of atomizing the sequence delivered by the supplied
     * iterator
     * @param base the supplied iterator, the input to atomization
     * @param untyped this can safely be set to true if it is known that all nodes in the base sequence will
     * be untyped; but it is always OK to set it to false.
     * @return an iterator that returns atomic values, the result of the atomization
     * @throws net.sf.saxon.trans.XPathException if a dynamic evaluation error occurs
     */

    public static SequenceIterator<? extends AtomicValue> getAtomizingIterator(SequenceIterator base, boolean untyped) throws XPathException {
        if (base instanceof AxisIterator) {
            return new AxisAtomizingIterator((AxisIterator)base);
        }
        if ((base.getProperties() & SequenceIterator.LAST_POSITION_FINDER) != 0) {
            int count = ((LastPositionFinder)base).getLength();
            if (count == 0) {
                return EmptyIterator.emptyIterator();
            } else if (count == 1) {
                Item first = base.next();
                assert first != null;
                return first.getTypedValue();
            }
        }
        //return new MappingIterator(base, AtomizingFunction.getInstance());
        if (untyped) {
            return new UntypedAtomizingIterator(base);
        } else {
            return new AtomizingIterator(base);
        }
    }



    /**
     * Diagnostic print of expression structure. The abstract expression tree
     * is written to the supplied output destination.
     */

    public String getExpressionName() {
        return "data";
    }

    /**
    * Implement the mapping function. This is stateless, so there is a singleton instance.
    */

    public static class AtomizingFunction implements MappingFunction<Item, AtomicValue> {

        /**
         * Private constructor, ensuring that everyone uses the singleton instance
         */

        private AtomizingFunction(){}

        private static final AtomizingFunction theInstance = new AtomizingFunction();

        /**
         * Get the singleton instance
         * @return the singleton instance of this mapping function
         */

        public static AtomizingFunction getInstance() {
            return theInstance;
        }

        public SequenceIterator<AtomicValue> map(Item item) throws XPathException {
            if (item instanceof NodeInfo) {
                return item.getTypedValue();
            } else {
                return SingletonIterator.makeIterator((AtomicValue)item);
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