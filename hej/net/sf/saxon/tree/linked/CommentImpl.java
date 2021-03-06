package net.sf.saxon.tree.linked;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Value;

/**
  * CommentImpl is an implementation of a Comment node
  * @author Michael H. Kay
  */


final class CommentImpl extends NodeImpl {

    String comment;

    public CommentImpl(String content) {
        this.comment = content;
    }

    public final String getStringValue() {
        return comment;
    }

    /**
     * Get the typed value of this node.
     * Returns the string value, as an instance of xs:string
     */

    /*@NotNull*/ public SequenceIterator getTypedValue() {
        return SingletonIterator.makeIterator(new StringValue(getStringValue()));
    }

    /**
     * Get the typed value of this node.
     * Returns the string value, as an instance of xs:string
     */

    /*@NotNull*/ public Value atomize() {
        return new StringValue(getStringValue());
    }        

    public final int getNodeKind() {
        return Type.COMMENT;
    }

    /**
    * Copy this node to a given outputter
    */

    public void copy(/*@NotNull*/ Receiver out, int copyOptions, int locationId) throws XPathException {
        out.comment(comment, locationId, 0);
    }


    /**
     * Replace the string-value of this node
     *
     * @param stringValue the new string value
     */

    public void replaceStringValue(/*@NotNull*/ CharSequence stringValue) {
        comment = stringValue.toString();
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