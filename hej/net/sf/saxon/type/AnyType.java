package net.sf.saxon.type;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.sort.IntHashSet;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Value;

import java.io.Serializable;

/**
 * This class has a singleton instance which represents the XML Schema built-in type xs:anyType,
 * also known as the urtype.
 *
 * See XML Schema 1.1 Part 1 section 3.4.7
 */

public final class AnyType implements ComplexType, Serializable {

    /*@NotNull*/ private static AnyType theInstance = new AnyType();

    /**
     * Private constructor
     */
    private AnyType() {
        super();
    }

    /**
     * Get the singular instance of this class
     * @return the singular object representing xs:anyType
     */

    /*@NotNull*/ public static AnyType getInstance() {
        return theInstance;
    }

    /**
     * Get the local name of this type
     *
     * @return the local name of this type definition, if it has one. Return null in the case of an
     *         anonymous type.
     */

    /*@NotNull*/ public String getName() {
        return "anyType";
    }

    /**
     * Get the target namespace of this type
     *
     * @return the target namespace of this type definition, if it has one. Return null in the case
     *         of an anonymous type, and in the case of a global type defined in a no-namespace schema.
     */

    public String getTargetNamespace() {
        return NamespaceConstant.SCHEMA;
    }

    /**
     * Get the variety of this complex type. This will be one of the values
     * {@link #VARIETY_EMPTY}, {@link #VARIETY_MIXED}, {@link #VARIETY_SIMPLE}, or
     * {@link #VARIETY_ELEMENT_ONLY}
     */

    public int getVariety() {
        return VARIETY_MIXED;
    }    

    /**
     * Get the validation status - always valid
     */
    public int getValidationStatus()  {
        return VALIDATED;
    }

    /**
     * Get the redefinition level. This is zero for a component that has not been redefined;
     * for a redefinition of a level-0 component, it is 1; for a redefinition of a level-N
     * component, it is N+1. This concept is used to support the notion of "pervasive" redefinition:
     * if a component is redefined at several levels, the top level wins, but it is an error to have
     * two versions of the component at the same redefinition level.
     * @return the redefinition level
     */

    public int getRedefinitionLevel() {
        return 0;
    }

    /**
     * Get the base type
     * @return null (this is the root of the type hierarchy)
     */

    /*@Nullable*/ public SchemaType getBaseType() {
        return null;
    }

    /**
     * Returns the base type that this type inherits from. This method can be used to get the
     * base type of a type that is known to be valid.
     * If this type is a Simpletype that is a built in primitive type then null is returned.
     *
     * @return the base type.
     * @throws IllegalStateException if this type is not valid.
     */

    /*@Nullable*/ public SchemaType getKnownBaseType() throws IllegalStateException {
        return null;
    }

    /**
     * Gets the integer code of the derivation method used to derive this type from its
     * parent. Returns zero for primitive types.
     *
     * @return a numeric code representing the derivation method, for example
     * {@link SchemaType#DERIVATION_RESTRICTION}
     */

    public int getDerivationMethod() {
        return 0;
    }

    /**
     * Determines whether derivation (of a particular kind)
     * from this type is allowed, based on the "final" property
     *
     * @param derivation the kind of derivation, for example {@link SchemaType#DERIVATION_LIST}
     * @return true if this kind of derivation is allowed
     */

    public boolean allowsDerivation(int derivation) {
        return true;
    }

    /**
     * Test whether this ComplexType has been marked as abstract.
     * @return false: this class is not abstract.
     */

    public boolean isAbstract() {
        return false;
    }

    /**
     * Test whether this SchemaType is a complex type
     *
     * @return true if this SchemaType is a complex type
     */

    public boolean isComplexType() {
        return true;
    }

    /**
     * Test whether this is an anonymous type
     * @return true if this SchemaType is an anonymous type
     */

    public boolean isAnonymousType() {
        return false;
    }

    /**
     * Test whether this SchemaType is a simple type
     * @return true if this SchemaType is a simple type
     */

    public boolean isSimpleType() {
        return false;
    }

    /**
     * Test whether this SchemaType is an atomic type
     * @return true if this SchemaType is an atomic type
     */

    public boolean isAtomicType() {
        return false;
    }

    /**
     * Ask whether this type is an ID type. This is defined to be any simple type
     * who typed value may contain atomic values of type xs:ID: that is, it includes types derived
     * from ID by restriction, list, or union. Note that for a node to be treated
     * as an ID, its typed value must be a *single* atomic value of type ID; the type of the
     * node, however, can still allow a list.
     */

    public boolean isIdType() {
        return false;
    }

    /**
     * Ask whether this type is an IDREF or IDREFS type. This is defined to be any simple type
     * who typed value may contain atomic values of type xs:IDREF: that is, it includes types derived
     * from IDREF or IDREFS by restriction, list, or union
     */

    public boolean isIdRefType() {
        return false;
    }

    /**
     * Returns the value of the 'block' attribute for this type, as a bit-signnificant
     * integer with fields such as {@link SchemaType#DERIVATION_LIST} and {@link SchemaType#DERIVATION_EXTENSION}
     *
     * @return the value of the 'block' attribute for this type
     */

    public int getBlock() {
        return 0;
    }

    /**
     * Test whether this complex type has complex content
     * @return true: this complex type has complex content
     */
    public boolean isComplexContent() {
        return true;
    }

    /**
     * Test whether this complex type has simple content
     * @return false: this complex type has complex content
     */

    public boolean isSimpleContent() {
        return false;
    }

    /**
     * Test whether this complex type has "all" content, that is, a content model
     * using an xs:all compositor
     * @return false: this complex type does not use an "all" compositor
     */

    public boolean isAllContent() {
        return false;
    }

    /**
     * For a complex type with simple content, return the simple type of the content.
     * Otherwise, return null.
     * @return null: this complex type does not have simple content
     */

    /*@Nullable*/ public SimpleType getSimpleContentType() {
        return null;
    }

    /**
     * Test whether this complex type is derived by restriction
     * @return false: this type is not a restriction
     */
    public boolean isRestricted() {
        return false;
    }

    /**
     * Test whether the content type of this complex type is empty
     * @return false: the content model is not empty
     */

    public boolean isEmptyContent() {
        return false;
    }

    /**
     * Test whether the content model of this complexType allows empty content
     * @return true: the content is allowed to be empty
     */

    public boolean isEmptiable() {
        return true;
    }

    /**
     * Test whether this complex type allows mixed content
     * @return true: mixed content is allowed
     */

    public boolean isMixedContent() {
        return true;
    }

    /**
     * Get the fingerprint of the name of this type
     * @return the fingerprint.
     */

    public int getFingerprint() {
        return StandardNames.XS_ANY_TYPE;
    }

    /**
     * Get the namecode of the name of this type. This includes the prefix from the original
     * type declaration: in the case of built-in types, there may be a conventional prefix
     * or there may be no prefix.
     */

    public int getNameCode() {
        return StandardNames.XS_ANY_TYPE;
    }

    /**
     * Get a description of this type for use in diagnostics
     * @return the string "xs:anyType"
     */

    /*@NotNull*/ public String getDescription() {
        return "xs:anyType";
    }

    /**
     * Get the display name of the type: that is, a lexical QName with an arbitrary prefix
     *
     * @return a lexical QName identifying the type
     */

    /*@NotNull*/ public String getDisplayName() {
        return "xs:anyType";
    }


    /**
     * Get the URI of the schema document containing the definition of this type
     * @return null for a built-in type
     */

    /*@Nullable*/ public String getSystemId() {
        return null;
    }

    /**
     * Test whether this is the same type as another type. They are considered to be the same type
     * if they are derived from the same type definition in the original XML representation (which
     * can happen when there are multiple includes of the same file)
     */

    public boolean isSameType(SchemaType other) {
        return (other instanceof AnyType);
    }

    /**
     * Analyze an expression to see whether the expression is capable of delivering a value of this
     * type.
     *
     @param expression the expression that delivers the content
     * @param kind       the node kind whose content is being delivered: {@link net.sf.saxon.type.Type#ELEMENT},
          *                   {@link net.sf.saxon.type.Type#ATTRIBUTE}, or {@link net.sf.saxon.type.Type#DOCUMENT}
     * @param env

     */

    public void analyzeContentExpression(Expression expression, int kind, StaticContext env) {
        //return;
    }

    /**
     * Get the typed value of a node that is annotated with this schema type
     * @param node the node whose typed value is required
     * @return an iterator returning a single untyped atomic value, equivalent to the string value of the node. This
     * follows the standard rules for elements with mixed content.
     */

    /*@NotNull*/ public SequenceIterator getTypedValue(/*@NotNull*/ NodeInfo node) {
        return SingletonIterator.makeIterator(new UntypedAtomicValue(node.getStringValue()));
    }

    /**
     * Get the typed value of a node that is annotated with this schema type. The result of this method will always be consistent with the method
     * {@link #getTypedValue}. However, this method is often more convenient and may be
     * more efficient, especially in the common case where the value is expected to be a singleton.
     *
     * @param node the node whose typed value is required
     * @return the typed value.
     * @since 8.5
     */

    /*@NotNull*/ public Value atomize(/*@NotNull*/ NodeInfo node) {
        return new UntypedAtomicValue(node.getStringValue());
    }

    /**
     * Test whether this complex type subsumes another complex type. The algorithm
     * used is as published by Thompson and Tobin, XML Europe 2003.
     * @param sub the other type (the type that is derived by restriction, validly or otherwise)
     * @param compiler
     * @return null indicating that this type does indeed subsume the other; or a string indicating
     * why it doesn't.
     */

//    public String subsumes(ComplexType sub, ISchemaCompiler compiler) {
//        return null;
//    }

    /**
     * Check that this type is validly derived from a given type
     *
     * @param type  the type from which this type is derived
     * @param block the derivations that are blocked by the relevant element declaration
     * @throws SchemaException
     *          if the derivation is not allowed
     */

    public void checkTypeDerivationIsOK(SchemaType type, int block) throws SchemaException {
        if (!(type instanceof AnyType)) {
            throw new SchemaException("Cannot derive xs:anyType from another type");
        }
    }

    /**
     * Find an element particle within this complex type definition having a given element name
     * (identified by fingerprint), and return the schema type associated with that element particle.
     * If there is no such particle, return null. If the fingerprint matches an element wildcard,
     * return the type of the global element declaration with the given name if one exists, or AnyType
     * if none exists and lax validation is permitted by the wildcard.
     *
     * @param fingerprint Identifies the name of the child element within this content model
     * @param considerExtensions
     */

    /*@NotNull*/ public SchemaType getElementParticleType(int fingerprint, boolean considerExtensions) {
        return this;
    }

    /**
     * Find an element particle within this complex type definition having a given element name
     * (identified by fingerprint), and return the cardinality associated with that element particle,
     * that is, the number of times the element can occur within this complex type. The value is one of
     * {@link net.sf.saxon.expr.StaticProperty#EXACTLY_ONE}, {@link net.sf.saxon.expr.StaticProperty#ALLOWS_ZERO_OR_ONE},
     * {@link net.sf.saxon.expr.StaticProperty#ALLOWS_ZERO_OR_MORE}, {@link net.sf.saxon.expr.StaticProperty#ALLOWS_ONE_OR_MORE},
     * If there is no such particle, return zero.
     *
     * @param fingerprint Identifies the name of the child element within this content model
     * @param considerExtensions
     */

    public int getElementParticleCardinality(int fingerprint, boolean considerExtensions) {
        return StaticProperty.ALLOWS_ZERO_OR_MORE;
    }

    /**
     * Find an attribute use within this complex type definition having a given attribute name
     * (identified by fingerprint), and return the schema type associated with that attribute.
     * If there is no such attribute use, return null. If the fingerprint matches an attribute wildcard,
     * return the type of the global attribute declaration with the given name if one exists, or AnySimpleType
     * if none exists and lax validation is permitted by the wildcard.
     *
     * @param fingerprint Identifies the name of the child element within this content model
     */

    /*@NotNull*/ public SimpleType getAttributeUseType(int fingerprint) {
        return AnySimpleType.getInstance();
    }

    /**
     * Find an attribute use within this complex type definition having a given attribute name
     * (identified by fingerprint), and return the cardinality associated with that attribute,
     * which will always be 0, 1, or 0-or-1.
     * If there is no such attribute use, return null. If the fingerprint matches an attribute wildcard,
     * return the type of the global attribute declaration with the given name if one exists, or AnySimpleType
     * if none exists and lax validation is permitted by the wildcard.
     * <p/>
     * If there are types derived from this type by extension, search those too.
     * @param fingerprint Identifies the name of the child element within this content model
     * @return the schema type associated with the attribute use identified by the fingerprint.
     *         If there is no such attribute use, return null.
     */

    public int getAttributeUseCardinality(int fingerprint) throws SchemaException {
        return StaticProperty.ALLOWS_ZERO_OR_ONE;
    }

    /**
     * Return true if this type (or any known type derived from it by extension) allows the element
     * to have one or more attributes.
     * @return true if attributes are allowed
     */

    public boolean allowsAttributes() {
        return true;
    }

    /**
     * Get a list of all the names of elements that can appear as children of an element having this
     * complex type, as integer fingerprints. If the list is unbounded (because of wildcards or the use
     * of xs:anyType), return null.
     *
     * @param children an integer set, initially empty, which on return will hold the fingerprints of all permitted
     *                 child elements; if the result contains the value -1, this indicates that it is not possible to enumerate
     *                 all the children, typically because of wildcards. In this case the other contents of the set should
     * @param ignoreWildcards
     */

     public void gatherAllPermittedChildren(/*@NotNull*/ IntHashSet children, boolean ignoreWildcards) throws SchemaException {
        children.add(-1);
    }

    /**
     * Get a list of all the names of elements that can appear as descendants of an element having this
     * complex type, as integer fingerprints. If the list is unbounded (because of wildcards or the use
     * of xs:anyType), return null.
     *
     * @param descendants an integer set, initially empty, which on return will hold the fingerprints of all permitted
     *                    descendant elements; if the result contains the value -1, this indicates that it is not possible to enumerate
     *                    all the descendants, typically because of wildcards. In this case the other contents of the set should
     *                    be ignored.
     */

    public void gatherAllPermittedDescendants(/*@NotNull*/ IntHashSet descendants) throws SchemaException {
        descendants.add(-1);
    }

    /**
     * Assuming an element is a permitted descendant in the content model of this type, determine
     * the type of the element when it appears as a descendant. If it appears with more than one type,
     * return xs:anyType.
     * @param fingerprint the name of the required descendant element
     * @return the type of the descendant element; null if the element cannot appear as a descendant;
     *         anyType if it can appear with several different types
     */

    /*@NotNull*/ public SchemaType getDescendantElementType(int fingerprint) throws SchemaException {
        return this;
    }

    /**
     * Assuming an element is a permitted descendant in the content model of this type, determine
     * the cardinality of the element when it appears as a descendant.
     * @param fingerprint the name of the required descendant element
     * @return the cardinality of the descendant element within this complex type
     */

    public int getDescendantElementCardinality(int fingerprint) throws SchemaException {
        return StaticProperty.ALLOWS_ZERO_OR_MORE;
    }

    /**
     * Ask whether this type (or any known type derived from it by extension) allows the element
     * to have children that match a wildcard
     * @return true if the content model of this type, or its extensions, contains an element wildcard
     */

    public boolean containsElementWildcard() {
        return true;
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