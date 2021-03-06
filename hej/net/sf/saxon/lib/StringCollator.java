package net.sf.saxon.lib;

import java.io.Serializable;

/**
 * This interface represents a "collation" as defined in XPath, that is, a set of rules for comparing strings
 * <p>Note: an implementation of this interface that wraps a Java {@link java.text.RuleBasedCollator} is
 * available: see {@link net.sf.saxon.expr.sort.RuleBasedSubstringMatcher}.</p>
 */
public interface StringCollator extends Serializable {

    /**
     * Compare two strings
     * @param o1 the first string
     * @param o2 the second string
     * @return 0 if the strings are considered equal, a negative integer if the first string is less than the second,
     * a positive integer if the first string is greater than the second
     */

    int compareStrings(String o1, String o2);

    /**
     * Compare two strings for equality. This may be more efficient than using compareStrings and
     * testing whether the result is zero, but it must give the same result
     * @param s1 the first string
     * @param s2 the second string
     * @return true if and only if the strings are considered equal,
     */

    boolean comparesEqual(String s1, String s2);

    /**
     * Get a collation key for a String. The essential property of collation keys
     * is that if (and only if) two strings are equal under the collation, then
     * comparing the collation keys using the equals() method must return true.
     * @param s the string whose collation key is required
     * @return the collation key
     */

    Object getCollationKey(String s);


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