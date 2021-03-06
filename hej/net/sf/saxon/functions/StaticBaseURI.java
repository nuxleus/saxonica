package net.sf.saxon.functions;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;

/**
 * This class supports the static-base-uri() function in XPath 2.0. The expressio
 * is always evaluated at compile time.
*/

public class StaticBaseURI extends CompileTimeFunction {

    /**
    * Compile time evaluation
     * @param visitor an expression visitor
     */

    public Expression preEvaluate(/*@NotNull*/ ExpressionVisitor visitor) throws XPathException {
        String baseURI = visitor.getStaticContext().getBaseURI();
        if (baseURI == null) {
            return Literal.makeEmptySequence();
        }
        return Literal.makeLiteral(new AnyURIValue(baseURI));
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