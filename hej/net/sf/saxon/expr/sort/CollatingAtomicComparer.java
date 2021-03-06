package net.sf.saxon.expr.sort;

import net.sf.saxon.Platform;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.value.AtomicValue;

/**
 * An AtomicComparer used for comparing strings, untypedAtomic values, and URIs using a collation.
 * A CollatingAtomicComparer is used when it is known in advance that both operands will be of these
 * types. This enables all conversions and promotions to be bypassed: the string values of both operands
 * are simply extracted and passed to the collator for comparison.
 *
 * @author Michael H. Kay
 *
 */

public class CollatingAtomicComparer implements AtomicComparer {

    private StringCollator collator;
    private boolean canReturnCollationKeys;

    /**
     * Create an GenericAtomicComparer
     * @param collator the collation to be used. If the method is called at compile time, this should
     * be a SimpleCollation so that it can be cloned at run-time.
     * @param platform used to obtain collation keys.
     */

    public CollatingAtomicComparer(/*@Nullable*/ StringCollator collator, Platform platform) {

        if (collator == null) {
            this.collator = CodepointCollator.getInstance();
        } else {
            this.collator = collator;
        }
        canReturnCollationKeys = platform.canReturnCollationKeys(this.collator);
    }


    public StringCollator getCollator() {
        return collator;
    }

    /**
     * Supply the dynamic context in case this is needed for the comparison
     *
     * @param context the dynamic evaluation context
     * @return either the original AtomicComparer, or a new AtomicComparer in which the context
     *         is known. The original AtomicComparer is not modified
     */

    public AtomicComparer provideContext(XPathContext context) {
        return this;
    }


    /**
    * Compare two AtomicValue objects according to the rules for their data type. UntypedAtomic
    * values are compared as if they were strings; if different semantics are wanted, the conversion
    * must be done by the caller.
    * @param a the first object to be compared. It is intended that this should be an instance
    * of AtomicValue, though this restriction is not enforced. If it is a StringValue, the
    * collator is used to compare the values, otherwise the value must implement the java.util.Comparable
    * interface.
    * @param b the second object to be compared. This must be comparable with the first object: for
    * example, if one is a string, they must both be strings.
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    * @throws ClassCastException if the objects are not comparable
    */

    public int compareAtomicValues(AtomicValue a, AtomicValue b) {
        if (a == null) {
            if (b == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (b == null) {
            return +1;
        }

        return collator.compareStrings(a.getStringValue(), b.getStringValue());
    }

    /**
    * Compare two AtomicValue objects for equality according to the rules for their data type. UntypedAtomic
    * values are compared by converting to the type of the other operand.
    * @param a the first object to be compared. It is intended that this should be an instance
    * of AtomicValue, though this restriction is not enforced. If it is a StringValue, the
    * collator is used to compare the values, otherwise the value must implement the equals() method.
    * @param b the second object to be compared. This must be comparable with the first object: for
    * example, if one is a string, they must both be strings.
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    * @throws ClassCastException if the objects are not comparable
    */

    public boolean comparesEqual(AtomicValue a, AtomicValue b) {
        return compareAtomicValues(a, b) == 0;
    }

    /**
    * Get a comparison key for an object. This must satisfy the rule that if two objects are equal,
    * then their comparison keys are equal, and vice versa. There is no requirement that the
    * comparison keys should reflect the ordering of the underlying objects.
    */

    public ComparisonKey getComparisonKey(AtomicValue a) {
        if (canReturnCollationKeys) {
            return new ComparisonKey(StandardNames.XS_STRING, collator.getCollationKey(a.getStringValue()));
        } else {
            return new ComparisonKey(StandardNames.XS_STRING, a.getStringValue());
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