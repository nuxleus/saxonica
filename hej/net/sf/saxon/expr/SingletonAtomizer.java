package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RoleLocator;
import net.sf.saxon.om.FunctionItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.EmptySequenceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Value;

/**
* A SingletonAtomizer combines the functions of an Atomizer and a CardinalityChecker: it is used to
 * atomize a sequence of nodes, checking that the result of the atomization contains zero or one atomic
 * values. Note that the input may be a sequence of nodes or atomic values, even though the result must
 * contain at most one atomic value.
*/

public final class SingletonAtomizer extends UnaryExpression {

    private boolean allowEmpty;
    private RoleLocator role;

    /**
    * Constructor
     * @param sequence the sequence to be atomized
     * @param role contains information about where the expression appears, for use in any error message
     * @param allowEmpty true if the result sequence is allowed to be empty.
    */

    public SingletonAtomizer(Expression sequence, RoleLocator role, boolean allowEmpty) {
        super(sequence);
        this.allowEmpty = allowEmpty;
        this.role = role;
    }

    /**
    * Simplify an expression
     * @param visitor an expression visitor
     */

     /*@NotNull*/
     public Expression simplify(ExpressionVisitor visitor) throws XPathException {
        operand = visitor.simplify(operand);
        if (operand instanceof Literal && ((Literal)(operand)).getValue() instanceof AtomicValue) {
            return operand;
        }
        return this;
    }

    /**
    * Type-check the expression
    */

    /*@NotNull*/
    public Expression typeCheck(ExpressionVisitor visitor, ExpressionVisitor.ContextItemType contextItemType) throws XPathException {
        operand = visitor.typeCheck(operand, contextItemType);
        visitor.resetStaticProperties();
        if (Literal.isEmptySequence(operand)) {
            if (!allowEmpty) {
                typeError("An empty sequence is not allowed as the " +
                        role.getMessage(), role.getErrorCode(), null);
            }
            return operand;
        }
        final TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (operand.getItemType(th).isPlainType()) {
            return operand;
        }
        return this;
    }


    /*@NotNull*/
    public Expression optimize(ExpressionVisitor visitor, ExpressionVisitor.ContextItemType contextItemType) throws XPathException {
        Expression exp = super.optimize(visitor, contextItemType);
        if (exp == this) {
            final TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
            if (operand.getItemType(th).isPlainType()) {
                return operand;
            }
            return this;
        } else {
            return exp;
        }
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
        return new SingletonAtomizer(getBaseExpression().copy(), role, allowEmpty);
    }

    /**
     * Get the RoleLocator (used to construct error messages)
     * @return the role locator
     */

    public RoleLocator getRole() {
        return role;
    }


    /*@Nullable*/ public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
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
    * Evaluate as an Item. This should only be called if a singleton or empty sequence is required;
     * it throws a type error if the underlying sequence is multi-valued.
    */

    public Item evaluateItem(XPathContext context) throws XPathException {
        int found = 0;
        Item result = null;
        SequenceIterator iter = operand.iterate(context);
        while (true) {
            Item item = iter.next();
            if (item == null) {
                break;
            }
            if (item instanceof AtomicValue) {
                if (found++ > 0) {
                    typeError(
                        "A sequence of more than one item is not allowed as the " +
                        role.getMessage(), role.getErrorCode(), context);
                }
                result = item;
            } else if (item instanceof FunctionItem) {
                typeError("A function item cannot appear as the " +
                        role.getMessage(), "FOTY0013", context);
            } else {
                Value value = ((NodeInfo)item).atomize();
                found += value.getLength();
                if (found > 1) {
                     typeError(
                        "A sequence of more than one item is not allowed as the " +
                        role.getMessage(), role.getErrorCode(), context);
                }
                result = value.itemAt(0);
            }
        }
        if (found == 0 && !allowEmpty) {
            typeError("An empty sequence is not allowed as the " +
                             role.getMessage(), role.getErrorCode(), null);
        }
        return result;
    }

    /**
    * Determine the data type of the items returned by the expression, if possible
    * @return a value such as Type.STRING, Type.BOOLEAN, Type.NUMBER. For this class, the
     * result is always an atomic type, but it might be more specific.
     * @param th the type hierarchy cache
     */

	/*@NotNull*/
    public ItemType getItemType(TypeHierarchy th) {
        boolean isSchemaAware = true;
        try {
            isSchemaAware = getExecutable().isSchemaAware();
        } catch (NullPointerException err) {
            // ultra-cautious code in case expression container has not been set
            if (!th.getConfiguration().isLicensedFeature(Configuration.LicenseFeature.SCHEMA_VALIDATION)) {
                isSchemaAware = false;
            }
        }
        ItemType in = operand.getItemType(th);
        if (in.isPlainType()) {
            return in;
        }
        if (in instanceof NodeTest) {

            if (in instanceof EmptySequenceTest) {
                return in;
            }

            int kinds = ((NodeTest)in).getNodeKindMask();
            if (!isSchemaAware) {
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
        if (allowEmpty) {
            return StaticProperty.ALLOWS_ZERO_OR_ONE;
        } else {
            return StaticProperty.EXACTLY_ONE;
        }
	}

    /**
     * Give a string representation of the expression name for use in diagnostics
     * @return the expression name, as a string
     */

    public String getExpressionName() {
        return "atomizeSingleton";
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