package net.sf.saxon.om;


import net.sf.saxon.trans.XPathException;

/**
 * Interface for deciding whether a particular element is to have whitespace text nodes stripped
 */

public interface SpaceStrippingRule {

    /**
    * Decide whether an element is in the set of white-space preserving element types
    *
     *
     * @param nodeName Identifies the name of the element whose whitespace is to
      * be preserved
      * @return {@link net.sf.saxon.event.Stripper#ALWAYS_PRESERVE} if the element is in the set of white-space preserving
     *  element types, {@link net.sf.saxon.event.Stripper#ALWAYS_STRIP} if the element is to be stripped regardless of the
     * xml:space setting, and {@link net.sf.saxon.event.Stripper#STRIP_DEFAULT} otherwise
     * @throws net.sf.saxon.trans.XPathException if the rules are ambiguous and ambiguities are to be
     * reported as errors
    */

    public abstract byte isSpacePreserving(NodeName nodeName) throws XPathException;

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
