package net.sf.saxon.type;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.trans.XPathException;

import java.io.Serializable;


/**
 * ItemType is an interface that allows testing of whether an Item conforms to an
 * expected type. ItemType represents the types in the type hierarchy in the XPath model,
 * as distinct from the schema model: an item type is either item() (matches everything),
 * a node type (matches nodes), an atomic type (matches atomic values), or empty()
 * (matches nothing). Atomic types, represented by the class AtomicType, are also
 * instances of SimpleType in the schema type hierarchy. Node Types, represented by
 * the class NodeTest, are also Patterns as used in XSLT.
 *
 * <p>Saxon assumes that apart from {@link AnyItemType} (which corresponds to <code>item()</item>
 * and matches anything), every ItemType will be either an {@link AtomicType}, a {@link net.sf.saxon.pattern.NodeTest},
 * or a {@link FunctionItemType}. User-defined implementations of ItemType must therefore extend one of those
 * three classes/interfaces.</p>
 * @see AtomicType
 * @see net.sf.saxon.pattern.NodeTest
 * @see FunctionItemType
*/

public interface ItemType extends Serializable {

    /**
     * Determine whether this item type is an atomic type
     * @return true if this is ANY_ATOMIC_TYPE or a subtype thereof
     */

    public boolean isAtomicType();

    /**
     * Determine whether this item type is a plain type (that is, whether it can ONLY match
     * atomic values)
     * @return true if this is ANY_ATOMIC_TYPE or a subtype thereof, or a
     * "plain" union type (that is, unions of atomic types that impose no further restrictions)
     */

    public boolean isPlainType();

    /**
     * Test whether a given item conforms to this type
     * @param item The item to be tested
     * @param context the XPath dynamic evaluation context
     * @return true if the item is an instance of this type; false otherwise
    */

    public boolean matches(Item item, XPathContext context);


    /**
     * Test whether a given item conforms to this type
     * @param item The item to be tested
     * @param allowURIPromotion if a URI value is to be treated as a string
     * @param config the Saxon configuration
     * @return true if the item is an instance of this type; false otherwise
    */

    public boolean matchesItem(Item item, boolean allowURIPromotion, Configuration config);

    /**
     * Get the type from which this item type is derived by restriction. This
     * is the supertype in the XPath type heirarchy, as distinct from the Schema
     * base type: this means that the supertype of xs:boolean is xs:anyAtomicType,
     * whose supertype is item() (rather than xs:anySimpleType).
     * <p>
     * In fact the concept of "supertype" is not really well-defined, because the types
     * form a lattice rather than a hierarchy. The only real requirement on this function
     * is that it returns a type that strictly subsumes this type, ideally as narrowly
     * as possible.
     * @return the supertype, or null if this type is item()
     * @param th the type hierarchy cache
     */

    /*@Nullable*/ public ItemType getSuperType(TypeHierarchy th);

    /**
     * Get the primitive item type corresponding to this item type. For item(),
     * this is Type.ITEM. For node(), it is Type.NODE. For specific node kinds,
     * it is the value representing the node kind, for example Type.ELEMENT.
     * For anyAtomicValue and union types it is Type.ATOMIC_VALUE. For numeric it is Type.NUMBER.
     * For other atomic types it is the primitive type as defined in XML Schema,
     * except that integer, xs:dayTimeDuration, and xs:yearMonthDuration
     * are considered to be primitive types.
     * @return the corresponding primitive type
     */

    /*@NotNull*/
    public ItemType getPrimitiveItemType();

    /**
     * Get the primitive type corresponding to this item type. For item(),
     * this is Type.ITEM. For node(), it is Type.NODE. For specific node kinds,
     * it is the value representing the node kind, for example Type.ELEMENT.
     * For anyAtomicValue it is BuiltInAtomicType.ANY_ATOMIC. For numeric it is Type.NUMBER.
     * For other atomic types it is the primitive type as defined in XML Schema,
     * except that INTEGER is considered to be a primitive type.
     * @return the integer fingerprint of the corresponding primitive type
     */

    public int getPrimitiveType();

    /**
     * Determine the default priority of this item type when used on its own as a Pattern
     * @return the default priority
    */

    public abstract double getDefaultPriority();

    /**
     * Produce a representation of this type name for use in error messages.
     * Where this is a QName, it will use conventional prefixes
     * @param pool the name pool
     * @return a string representation of the type, in notation resembling but not necessarily
     * identical to XPath syntax
     */

    /*@Nullable*/ public String toString(NamePool pool);

    /**
     * Get the item type of the atomic values that will be produced when an item
     * of this type is atomized
     * @return  the best available item type of the atomic values that will be produced when an item
     * of this type is atomized
     */

    /*@NotNull*/ public PlainType getAtomizedItemType();

    /**
     * Ask whether values of this type are atomizable
     * @return true unless it is known that these items will be elements with element-only
     * content, or function items, in which case return false
     */

    public boolean isAtomizable();

    /**
     * Visit all the schema components used in this ItemType definition
     * @param visitor the visitor class to be called when each component is visited
     * @throws net.sf.saxon.trans.XPathException if an error occurs
     */

    public void visitNamedSchemaComponents(SchemaComponentVisitor visitor) throws XPathException;

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