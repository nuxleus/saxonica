package net.sf.saxon.functions;

import net.sf.saxon.expr.CallableExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.AnyURIValue;

/**
* This class supports the base-uri() function in XPath 2.0
*/

public class BaseURI extends SystemFunction implements CallableExpression {

    /**
     * Simplify and validate.
     * This is a pure function so it can be simplified in advance if the arguments are known
     * @param visitor an expression visitor
     */

     /*@NotNull*/
     public Expression simplify(ExpressionVisitor visitor) throws XPathException {
        useContextItemAsDefault(visitor);
        return simplifyArguments(visitor);
    }

    /**
    * Evaluate the function at run-time
    */

    /*@Nullable*/ public Item evaluateItem(XPathContext c) throws XPathException {
        NodeInfo node = (NodeInfo)argument[0].evaluateItem(c);
        if (node==null) {
            return null;
        }
        String s = node.getBaseURI();
        if (s == null) {
            return null;
        }
        return new AnyURIValue(s);
    }
    
    public SequenceIterator<AnyURIValue> call(SequenceIterator[] arguments, XPathContext context) throws XPathException{
    	 NodeInfo node = (NodeInfo)arguments[0].next();
         if (node==null) {
        	 return EmptyIterator.emptyIterator();
         }
         String s = node.getBaseURI();
         if (s == null) {
             return EmptyIterator.emptyIterator();
         }
         return SingletonIterator.makeIterator(new AnyURIValue(s));
    	
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