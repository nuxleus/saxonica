package net.sf.saxon.tree.tiny;

/**
 *  Defines a CharSequence to which characters can be appended
 */
public interface AppendableCharSequence extends CharSequence {

    /**
     * Append characters to this CharSequence
     * @param chars the characters to be appended
     */
    void append(CharSequence chars);

    /**
     * Set the length. If this exceeds the current length, this method is a no-op.
     * If this is less than the current length, characters beyond the specified point
     * are deleted.
     * @param length the new length
     */

    void setLength(int length);
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