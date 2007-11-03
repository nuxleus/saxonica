package net.sf.saxon.evpull;

import net.sf.saxon.trans.XPathException;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.type.Type;
import net.sf.saxon.Configuration;
import net.sf.saxon.tinytree.TinyNodeImpl;
import net.sf.saxon.tinytree.TinyTreeEventIterator;

/**
 * This class takes a sequence of pull events and turns it into fully-decomposed form, that is, it
 * takes and document and element nodes in the sequence and turns them into a subsequence consisting of a
 * start element|document event, a content sequence, and an end element|document event, recursively.
 *
 * <p>The resulting sequence is decomposed, but not flat (it will contain nested EventIterators). To flatten
 * it, use {@link EventStackIterator#flatten(EventIterator)} </p>
 */
public class Decomposer implements EventIterator {

    private EventIterator base;
    private Configuration config;

    /**
     * Create a Decomposer, which turns an event sequence into fully decomposed form
     * @param base the base sequence, which may be fully composed, fully decomposed, or
     * anything in between
     * @param config the Saxon configuration
     */

    public Decomposer(EventIterator base, Configuration config) {
        this.config = config;
        this.base = EventStackIterator.flatten(base);
    }

    /**
     * Create a Decomposer which returns the sequence of events corresponding to
     * a particular node
     * @param node the node to be decomposed
     */

    public Decomposer(NodeInfo node) {
        config = node.getConfiguration();
        base = new SingletonEventIterator(node);
    }

    /**
     * Get the next event in the sequence
     *
     * @return the next event, or null when the sequence is exhausted. Note that since an EventIterator is
     *         itself a PullEvent, this method may return a nested iterator.
     * @throws net.sf.saxon.trans.XPathException
     *          if a dynamic evaluation error occurs
     */

    public PullEvent next() throws XPathException {
        PullEvent pe = base.next();
        if (pe instanceof NodeInfo) {
            NodeInfo node = (NodeInfo)pe;
            switch (node.getNodeKind()) {
                case Type.DOCUMENT: {
                    if (node instanceof TinyNodeImpl) {
                        return new TinyTreeEventIterator(((TinyNodeImpl)node));
                    } else {
                        SequenceIterator content = node.iterateAxis(Axis.CHILD);
                        EventIterator contentEvents = new EventIteratorOverSequence(content);
                        return new BracketedDocumentIterator(
                                StartDocumentEvent.getInstance(),
                                new Decomposer(contentEvents, config),
                                EndDocumentEvent.getInstance());
                    }
                }
                case Type.ELEMENT: {
                    if (node instanceof TinyNodeImpl) {
                        return new TinyTreeEventIterator(((TinyNodeImpl)node));
                    } else {
                        SequenceIterator content = node.iterateAxis(Axis.CHILD);
                        EventIterator contentEvents = new EventIteratorOverSequence(content);
                        StartElementEvent see = new StartElementEvent(config);
                        see.setNameCode(node.getNameCode());
                        see.setTypeCode(node.getTypeAnnotation());
                        see.setLocalNamespaces(node.getDeclaredNamespaces(null));
                        AxisIterator atts = node.iterateAxis(Axis.ATTRIBUTE);
                        while (true) {
                            NodeInfo att = (NodeInfo)atts.next();
                            if (att == null) {
                                break;
                            }
                            see.addAttribute(att);
                        }
                        return new BracketedElementIterator(
                                see,
                                new Decomposer(contentEvents, config),
                                EndElementEvent.getInstance());
                    }
                }
                default:
                    return node;
            }
        } else {
            return pe;
        }
    }

    /**
     * Determine whether the EventIterator returns a flat sequence of events, or whether it can return
     * nested event iterators
     *
     * @return true if the next() method is guaranteed never to return an EventIterator
     */

    public boolean isFlatSequence() {
        return false;
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
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Contributor(s):
//

