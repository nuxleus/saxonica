package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionVisitor;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.functions.regex.RegexIterator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

/**
 *  An internal instruction used by the fn:analyze-string function to process a matching
 *  substring. The instruction generates the content of the fn:match output element, by
 *  generating startElement and endElement events at the start and end of a group.
 */
public class ProcessRegexMatchInstruction extends Instruction {

    int groupNameCode;
    int nrNameCode;

    public ProcessRegexMatchInstruction(NamePool namePool) {
        groupNameCode = namePool.allocate("fn", NamespaceConstant.FN, "group");
        nrNameCode = namePool.allocate("", "", "nr");
    }

    public int getIntrinsicDependencies() {
        return StaticProperty.DEPENDS_ON_FOCUS;
    }

    public TailCall processLeavingTail(XPathContext context) throws XPathException {
        SequenceIterator iter = context.getCurrentIterator();
        if (!(iter instanceof RegexIterator)) {
            throw new IllegalStateException("Current iterator should be a RegexIterator");
        }
        ((RegexIterator)iter).processMatchingSubstring(context, new RegexIterator.OnGroup() {
            
            public void onGroupStart(XPathContext c, int groupNumber) throws XPathException {
                Receiver out = c.getReceiver();
                out.startElement(groupNameCode, StandardNames.XS_UNTYPED, 0, 0);
                out.attribute(nrNameCode, StandardNames.XS_UNTYPED_ATOMIC, ""+groupNumber, 0, 0);
            }

            public void onGroupEnd(XPathContext c, int groupNumber) throws XPathException{
                Receiver out = c.getReceiver();
                out.endElement();
            }
        });
        return null;
    }

    public Expression simplify(ExpressionVisitor visitor) throws XPathException {
        return this;
    }

    public Expression copy() {
        // Safe to return "this", because the only fields are constant for a Configuration
        return this;
    }

    public void explain(ExpressionPresenter out) {
        out.startElement("processRegexMatchingSubstring");
        out.endElement();
    }
}

//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael Kay,
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//



