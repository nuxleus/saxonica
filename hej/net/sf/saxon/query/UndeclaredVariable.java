package net.sf.saxon.query;

import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.VariableDeclaration;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.trans.XPathException;

import java.util.Collections;
import java.util.Iterator;

/**
 *  An UndeclaredVariable object is created when a reference is encountered to a variable
 *  that has not yet been declared. This can happen as a result of recursive module imports.
 *  These references are resolved at the end of query parsing.
 */

public class UndeclaredVariable extends GlobalVariableDefinition {

    public UndeclaredVariable(){}

    public void transferReferences(VariableDeclaration var) {
        Iterator iter = references.iterator();
        while (iter.hasNext()) {
            BindingReference ref = (BindingReference)iter.next();
            var.registerReference(ref);
        }
        references = Collections.EMPTY_LIST;
    }

    /*@NotNull*/ public GlobalVariable compile(Executable exec, int slot) throws XPathException {
        throw new UnsupportedOperationException("Attempt to compile a place-holder for an undeclared variable");
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