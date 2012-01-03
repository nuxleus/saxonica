package net.sf.saxon.tree.iter;

import net.sf.saxon.expr.sort.DocumentOrderIterator;
import net.sf.saxon.expr.sort.GlobalOrderComparer;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

import javax.xml.transform.SourceLocator;
import java.util.ArrayList;
import java.util.List;

/**
 * An iterator that returns the same items as its base iterator, checking to see that they are either
 * all nodes, or all non-nodes; if they are all nodes, it delivers them in document order.
 */

public class HomogeneityCheckerIterator implements SequenceIterator {

    /*@Nullable*/ SequenceIterator base = null;
    SourceLocator loc;
    int state;
        // state = 0: initial state, will accept either nodes or atomic values
        // state = +1: have seen a node, all further items must be nodes
        // state = -1: have seen an atomic value or function item, all further items must be the same

    public HomogeneityCheckerIterator(SequenceIterator base, SourceLocator loc) throws XPathException {
        this.base = base;
        this.loc = loc;
        state = 0;
    }

    public void close() {
        base.close();
    }

    /*@Nullable*/ public Item current() {
        return base.current();
    }

    /*@NotNull*/
    public SequenceIterator getAnother() throws XPathException {
        // if we've got two iterators over the items, it's enough for one of them to do the homogeneity checking
        return base.getAnother();
    }

    /*@NotNull*/ private XPathException reportMixedItems() {
        XPathException err = new XPathException("Cannot mix nodes and atomic values in the result of a path expression");
        err.setErrorCode("XPTY0018");
        err.setLocator(loc);
        return err;
    }

    public int getProperties() {
        return 0;
    }

    /*@Nullable*/ public Item next() throws XPathException {
        Item item = base.next();
        if (item == null) {
            return null;
        }
        //first item in iterator
        if (state == 0) {
            if (item instanceof NodeInfo) {
                List<Item> nodes = new ArrayList<Item>(50);
                nodes.add(item);
                while ((item = base.next()) != null) {
                    if (!(item instanceof NodeInfo)) {
                        throw reportMixedItems();
                    } else {
                        nodes.add(item);
                    }
                }
                base = new DocumentOrderIterator(new ListIterator(nodes), GlobalOrderComparer.getInstance());
                state = 1; // first item is a node
                return base.next();
            } else {
                state = -1; // first item is an atomic value or function item
            }
        } else if (state == -1 && item instanceof NodeInfo) {
            throw reportMixedItems();
        }
        return item;
    }

    public int position() {
        return base.position();
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